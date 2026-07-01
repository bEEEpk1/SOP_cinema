import { useEffect, useMemo, useRef, useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';

type Movie = { id: number; title: string; description?: string; durationMinutes: number; ageRating: string; genre: string; active: boolean };
type Show = { id: number; movieId: number; hallId: number; startTime: string; endTime: string; basePrice: number; currency: string; status: string };
type Hall = { id: number; name: string; hallType: string; capacity: number; active: boolean };
type Seat = { id: number; hallId: number; rowNumber: number; seatNumber: number; seatType: string; active: boolean; availabilityStatus?: string };
type Customer = { id: number; email: string; phone: string; registered: boolean; createdAt?: string };
type Booking = { id: number; showId: number; seatId: number; customerId: number; customerEmail: string; status: string; reservedUntil: string; finalPrice: number; currency: string; paymentReference?: string; loyaltyPointsUsed: number };
type Ticket = { id: number; bookingId: number; ticketNumber: string; status: string; qrCode: string };
type AuditEntry = { sequenceNumber: number; eventId: string; eventType: string; source: string; occurredAt: string; processedAt: string; description: string };
type NotificationMessage = { type: string; eventId?: string; eventType?: string; title: string; message: string; level: string; icon?: string; source?: string; receivedAt?: string; activeConnections?: number };
type LoyaltySummary = { customerId: number; email: string; registered: boolean; balance: number };

type Page = 'movies' | 'booking' | 'my-bookings' | 'events';
type ApiError = { status?: number; message: string };

const CORE = import.meta.env.VITE_CORE_API_URL || 'http://localhost:8080';
const AUDIT = import.meta.env.VITE_AUDIT_API_URL || 'http://localhost:8081';
const NOTIFY = import.meta.env.VITE_NOTIFICATION_API_URL || 'http://localhost:8082';
const WS_URL = import.meta.env.VITE_NOTIFICATION_WS_URL || 'ws://localhost:8082/ws/notifications';
const MAILPIT = import.meta.env.VITE_MAILPIT_URL || 'http://localhost:8025';
const RABBITMQ = import.meta.env.VITE_RABBITMQ_URL || 'http://localhost:15672';

const SEAT_REFRESH_EVENTS = new Set([
  'booking.created',
  'booking.paid',
  'booking.cancelled',
  'booking.expired',
  'seat.released'
]);

function unwrap<T>(payload: any): T[] {
  if (Array.isArray(payload)) return payload as T[];
  if (payload?.entries && Array.isArray(payload.entries)) return payload.entries as T[];
  if (payload?.notifications && Array.isArray(payload.notifications)) return payload.notifications as T[];
  if (payload?._embedded) {
    const firstArray = Object.values(payload._embedded).find(Array.isArray) as any[] | undefined;
    return (firstArray || []).map((item) => item?.content || item) as T[];
  }
  return [];
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers || {})
    }
  });

  if (!response.ok) {
    let message = `${response.status} ${response.statusText}`;
    try {
      const body = await response.json();
      message = body.message || body.detail || body.error || message;
    } catch {
      // no body
    }
    throw { status: response.status, message } satisfies ApiError;
  }

  if (response.status === 204) return undefined as T;
  return response.json();
}

function fmtDate(value?: string) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('ru-RU', {
    day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
  }).format(new Date(value));
}

function money(value?: number, currency = 'RUB') {
  if (value === undefined || value === null) return '—';
  return `${Number(value).toLocaleString('ru-RU')} ${currency === 'RUB' ? '₽' : currency}`;
}

function statusRu(status?: string) {
  const map: Record<string, string> = {
    PENDING_PAYMENT: 'ожидает оплаты',
    PAID: 'оплачено',
    CANCELLED: 'отменено',
    EXPIRED: 'истекло',
    ACTIVE: 'активен',
    INVALID: 'недействителен',
    USED: 'использован',
    FREE: 'свободно',
    RESERVED: 'занято',
    OCCUPIED: 'занято',
    NOTIFIED: 'уведомлён',
    FULFILLED: 'исполнено',
    SCHEDULED: 'по расписанию'
  };
  return status ? (map[status] || status) : '—';
}

function isBookableShow(show?: Show | null) {
  if (!show) return false;
  const statusAllowed = ['SCHEDULED', 'OPEN', 'PLANNED'].includes(show.status || '');
  return statusAllowed && new Date(show.startTime).getTime() > Date.now();
}

function canRefund(show?: Show | null) {
  if (!show) return false;
  const deadlineMs = new Date(show.startTime).getTime() - 45 * 60 * 1000;
  return Date.now() < deadlineMs;
}

export default function App() {
  const [page, setPage] = useState<Page>('movies');
  const [movies, setMovies] = useState<Movie[]>([]);
  const [shows, setShows] = useState<Show[]>([]);
  const [halls, setHalls] = useState<Hall[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [audit, setAudit] = useState<AuditEntry[]>([]);
  const [history, setHistory] = useState<NotificationMessage[]>([]);
  const [live, setLive] = useState<NotificationMessage[]>([]);
  const [wsState, setWsState] = useState<'connected' | 'connecting' | 'offline'>('connecting');
  const [wsActiveConnections, setWsActiveConnections] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState('');
  const [error, setError] = useState('');

  const [selectedMovieId, setSelectedMovieId] = useState<number | null>(null);
  const [selectedShowId, setSelectedShowId] = useState<number | null>(null);
  const selectedShowIdRef = useRef<number | null>(null);
  const selectedCustomerIdRef = useRef<number | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const seatRefreshTimerRef = useRef<number | null>(null);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [selectedSeatId, setSelectedSeatId] = useState<number | null>(null);
  const [seatsByShowId, setSeatsByShowId] = useState<Record<number, Seat[]>>({});
  const [customerId, setCustomerId] = useState<number>(2);
  const [loyaltyPoints, setLoyaltyPoints] = useState<number>(0);
  const [loyaltySummary, setLoyaltySummary] = useState<LoyaltySummary | null>(null);
  const [createdBooking, setCreatedBooking] = useState<Booking | null>(null);
  const [createdTicket, setCreatedTicket] = useState<Ticket | null>(null);

  const selectedMovie = movies.find((m) => m.id === selectedMovieId) || null;
  const selectedShow = shows.find((s) => s.id === selectedShowId) || null;
  const selectedSeat = seats.find((s) => s.id === selectedSeatId) || null;
  const selectedCustomer = customers.find((c) => c.id === customerId) || customers[0];
  const availableLoyaltyPoints = selectedCustomer?.registered ? (loyaltySummary?.balance || 0) : 0;
  const normalizedLoyaltyPoints = selectedCustomer?.registered ? Math.min(Math.max(loyaltyPoints || 0, 0), availableLoyaltyPoints) : 0;

  const showsForMovie = useMemo(() => {
    return shows
      .filter((show) => isBookableShow(show))
      .filter((show) => !selectedMovieId || show.movieId === selectedMovieId)
      .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
  }, [shows, selectedMovieId]);

  const rows = useMemo(() => {
    const grouped = new Map<number, Seat[]>();
    for (const seat of seats) {
      if (!grouped.has(seat.rowNumber)) grouped.set(seat.rowNumber, []);
      grouped.get(seat.rowNumber)!.push(seat);
    }
    return Array.from(grouped.entries())
      .sort(([a], [b]) => a - b)
      .map(([row, list]) => [row, list.sort((a, b) => a.seatNumber - b.seatNumber)] as const);
  }, [seats]);

  useEffect(() => {
    selectedShowIdRef.current = selectedShowId;
  }, [selectedShowId]);

  useEffect(() => {
    selectedCustomerIdRef.current = selectedCustomer?.id || null;
  }, [selectedCustomer?.id]);

  useEffect(() => {
    loadAll();
  }, []);

  useEffect(() => {
    const existingSocket = wsRef.current;
    if (existingSocket && (existingSocket.readyState === WebSocket.OPEN || existingSocket.readyState === WebSocket.CONNECTING)) {
      return;
    }

    const socket = new WebSocket(WS_URL);
    wsRef.current = socket;
    setWsState('connecting');
    socket.onopen = () => setWsState('connected');
    socket.onclose = () => {
      if (wsRef.current === socket) wsRef.current = null;
      setWsState('offline');
    };
    socket.onerror = () => setWsState('offline');
    socket.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data) as NotificationMessage;
        if (typeof message.activeConnections === 'number') setWsActiveConnections(message.activeConnections);
        setLive((prev) => [message, ...prev].slice(0, 10));
        setHistory((prev) => [message, ...prev].slice(0, 20));

        if (message.eventType && SEAT_REFRESH_EVENTS.has(message.eventType)) {
          scheduleSeatRefresh();
        }
        if (message.eventType?.startsWith('loyalty.') && selectedCustomerIdRef.current) {
          request<LoyaltySummary>(`${CORE}/api/customers/${selectedCustomerIdRef.current}/loyalty`)
            .then(setLoyaltySummary)
            .catch(() => undefined);
        }
      } catch {
        // ignore malformed dev message
      }
    };
    return () => {
      if (seatRefreshTimerRef.current) {
        window.clearTimeout(seatRefreshTimerRef.current);
        seatRefreshTimerRef.current = null;
      }
      if (wsRef.current === socket) wsRef.current = null;
      socket.close(1000, 'frontend unmounted');
    };
  }, []);

  useEffect(() => {
    if (selectedCustomer?.id) {
      loadLoyaltyBalance(selectedCustomer.id);
    }
  }, [selectedCustomer?.id, selectedCustomer?.registered]);

  async function loadLoyaltyBalance(customerIdToLoad: number) {
    const customer = customers.find((item) => item.id === customerIdToLoad);
    if (!customer?.registered) {
      setLoyaltySummary(customer ? { customerId: customer.id, email: customer.email, registered: false, balance: 0 } : null);
      setLoyaltyPoints(0);
      return;
    }
    try {
      const summary = await request<LoyaltySummary>(`${CORE}/api/customers/${customerIdToLoad}/loyalty`);
      setLoyaltySummary(summary);
      setLoyaltyPoints((current) => Math.min(Math.max(current, 0), summary.balance || 0));
    } catch {
      setLoyaltySummary({ customerId: customer.id, email: customer.email, registered: true, balance: 0 });
    }
  }

  function resetBookingFlow() {
    setSelectedSeatId(null);
    setCreatedBooking(null);
    setCreatedTicket(null);
    setError('');
  }

  function firstShowForMovie(movieId: number | null, sourceShows = shows) {
    if (!movieId) return null;
    return sourceShows
      .filter((show) => show.movieId === movieId && isBookableShow(show))
      .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())[0] || null;
  }

  function selectMovie(movieId: number, navigateToBooking = false) {
    setSelectedMovieId(movieId);
    const firstShow = firstShowForMovie(movieId);
    setSelectedShowId(firstShow?.id || null);
    setSeats([]);
    resetBookingFlow();
    if (navigateToBooking) setPage('booking');
  }

  function selectShow(showId: number) {
    const show = shows.find((item) => item.id === showId);
    if (!show) return;
    setSelectedShowId(show.id);
    if (show.movieId !== selectedMovieId) setSelectedMovieId(show.movieId);
    resetBookingFlow();
  }

  function scheduleSeatRefresh() {
    if (!selectedShowIdRef.current) return;
    if (seatRefreshTimerRef.current) window.clearTimeout(seatRefreshTimerRef.current);
    seatRefreshTimerRef.current = window.setTimeout(() => {
      seatRefreshTimerRef.current = null;
      if (selectedShowIdRef.current) loadSeats(selectedShowIdRef.current, false);
      loadBookingsAndTickets();
    }, 250);
  }

  async function loadAll() {
    setLoading(true);
    setError('');
    try {
      const [moviesJson, showsJson, hallsJson, customersJson, bookingsJson, ticketsJson, auditJson, notificationsJson] = await Promise.all([
        request<any>(`${CORE}/api/movies?page=0&size=200`),
        request<any>(`${CORE}/api/shows?page=0&size=200`),
        request<any>(`${CORE}/api/halls?page=0&size=200`),
        request<any>(`${CORE}/api/customers?page=0&size=200`),
        request<any>(`${CORE}/api/bookings?page=0&size=200`),
        request<any>(`${CORE}/api/tickets?page=0&size=200`),
        request<any>(`${AUDIT}/api/audit?limit=50`).catch(() => ({ entries: [] })),
        request<any>(`${NOTIFY}/api/notifications?limit=20`).catch(() => ({ entries: [] }))
      ]);

      const moviesList = unwrap<Movie>(moviesJson);
      const showsList = unwrap<Show>(showsJson);
      setMovies(moviesList);
      setShows(showsList);
      setHalls(unwrap<Hall>(hallsJson));
      const customerList = unwrap<Customer>(customersJson);
      setCustomers(customerList);
      const bookingsList = unwrap<Booking>(bookingsJson);
      const ticketsList = unwrap<Ticket>(ticketsJson);
      setBookings(bookingsList);
      setTickets(ticketsList);
      await loadSeatsForBookings(bookingsList);
      setAudit(unwrap<AuditEntry>(auditJson));
      setHistory(unwrap<NotificationMessage>(notificationsJson));

      const nextMovieId = selectedMovieId && moviesList.some((movie) => movie.id === selectedMovieId)
        ? selectedMovieId
        : moviesList[0]?.id || null;

      const selectedShowStillValid = selectedShowId
        ? showsList.some((show) => show.id === selectedShowId && show.movieId === nextMovieId && isBookableShow(show))
        : false;

      const nextShowId = selectedShowStillValid
        ? selectedShowId
        : firstShowForMovie(nextMovieId, showsList)?.id || null;

      setSelectedMovieId(nextMovieId);
      setSelectedShowId(nextShowId);
      if (customerList.length && !customerList.some((customer) => customer.id === customerId)) {
        setCustomerId(customerList[0].id);
      }
    } catch (e: any) {
      setError(e.message || 'Не удалось загрузить данные');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (selectedShowId) loadSeats(selectedShowId);
  }, [selectedShowId]);

  async function loadSeats(showId: number, resetFlow = true) {
    if (resetFlow) {
      setSelectedSeatId(null);
      setCreatedBooking(null);
      setCreatedTicket(null);
    }
    try {
      const json = await request<any>(`${CORE}/api/shows/${showId}/seats?page=0&size=300`);
      setSeats(unwrap<Seat>(json));
    } catch (e: any) {
      setError(e.message || 'Не удалось загрузить места');
      setSeats([]);
    }
  }

  async function loadSeatsForBookings(bookingsList: Booking[]) {
    const showIds = Array.from(new Set(bookingsList.map((booking) => booking.showId))).slice(0, 30);
    const entries = await Promise.all(showIds.map(async (showId) => {
      try {
        const json = await request<any>(`${CORE}/api/shows/${showId}/seats?page=0&size=300`);
        return [showId, unwrap<Seat>(json)] as const;
      } catch {
        return [showId, [] as Seat[]] as const;
      }
    }));
    setSeatsByShowId(Object.fromEntries(entries));
  }

  async function loadBookingsAndTickets() {
    try {
      const [bookingsJson, ticketsJson] = await Promise.all([
        request<any>(`${CORE}/api/bookings?page=0&size=200`),
        request<any>(`${CORE}/api/tickets?page=0&size=200`)
      ]);
      const bookingsList = unwrap<Booking>(bookingsJson);
      setBookings(bookingsList);
      setTickets(unwrap<Ticket>(ticketsJson));
      await loadSeatsForBookings(bookingsList);
    } catch {
      // realtime refresh is best-effort
    }
  }

  async function createBooking() {
    if (!selectedShow || !selectedSeat || !selectedCustomer) {
      setError('Выберите сеанс, место и пользователя');
      return;
    }
    if (selectedMovieId && selectedShow.movieId !== selectedMovieId) {
      setError('Выбранный сеанс не относится к выбранному фильму. Выберите сеанс заново.');
      return;
    }
    if (!isBookableShow(selectedShow)) {
      setError('Этот сеанс уже начался, прошёл или недоступен для покупки.');
      return;
    }
    try {
      const json = await request<any>(`${CORE}/api/bookings`, {
        method: 'POST',
        body: JSON.stringify({
          showId: selectedShow.id,
          seatId: selectedSeat.id,
          customerId: selectedCustomer.id,
          customerEmail: selectedCustomer.email,
          loyaltyPointsUsed: normalizedLoyaltyPoints
        })
      });
      const booking = json.content || json;
      setCreatedBooking(booking);
      setToast(`Бронь #${booking.id} создана. Цена: ${money(booking.finalPrice, booking.currency)}`);
      await loadSeats(selectedShow.id, false);
      await loadAll();
    } catch (e: any) {
      setError(e.message || 'Не удалось создать бронь');
    }
  }

  async function payBooking(bookingId: number) {
    const booking = bookings.find((item) => item.id === bookingId) || createdBooking;
    const show = shows.find((item) => item.id === booking?.showId);
    if (show && new Date(show.startTime).getTime() <= Date.now()) {
      setError('Оплата недоступна: сеанс уже начался или прошёл.');
      return;
    }
    try {
      const json = await request<any>(`${CORE}/api/bookings/${bookingId}`, {
        method: 'PATCH',
        body: JSON.stringify({
          status: 'PAID',
          paymentReference: `front_pay_${Date.now()}`
        })
      });
      const booking = json.content || json;
      setCreatedBooking(booking);
      setToast(`Бронь #${booking.id} оплачена. События и письмо должны появиться автоматически.`);
      try {
        const ticketJson = await request<any>(`${CORE}/api/bookings/${booking.id}/ticket`);
        setCreatedTicket(ticketJson.content || ticketJson);
      } catch {
        setCreatedTicket(null);
      }
      await loadAll();
      if (selectedCustomer?.registered) await loadLoyaltyBalance(selectedCustomer.id);
      if (selectedShowId) await loadSeats(selectedShowId, false);
    } catch (e: any) {
      setError(e.message || 'Не удалось оплатить бронь');
    }
  }

  async function cancelBooking(bookingId: number) {
    const booking = bookings.find((item) => item.id === bookingId) || createdBooking;
    const show = shows.find((item) => item.id === booking?.showId);
    if (booking?.status === 'PAID' && !canRefund(show)) {
      setError('Возврат билета доступен только не позднее чем за 45 минут до начала сеанса.');
      return;
    }
    try {
      await request<void>(`${CORE}/api/bookings/${bookingId}`, { method: 'DELETE' });
      setToast(`Бронь #${bookingId} отменена. Место освобождено.`);
      await loadAll();
      if (selectedCustomer?.registered) await loadLoyaltyBalance(selectedCustomer.id);
      if (selectedShowId) await loadSeats(selectedShowId, false);
    } catch (e: any) {
      setError(e.message || 'Не удалось отменить бронь');
    }
  }

  async function joinWaitlist() {
    if (!selectedShow || !selectedSeat || !selectedCustomer) {
      setError('Выберите занятое место и пользователя');
      return;
    }
    if (!selectedCustomer.registered) {
      setError('Лист ожидания доступен только зарегистрированным пользователям.');
      return;
    }
    try {
      await request<any>(`${CORE}/api/waitlist`, {
        method: 'POST',
        body: JSON.stringify({
          showId: selectedShow.id,
          seatId: selectedSeat.id,
          customerId: selectedCustomer.id,
          customerEmail: selectedCustomer.email
        })
      });
      setToast('Запись в лист ожидания создана. При освобождении места придёт уведомление.');
      await loadAll();
    } catch (e: any) {
      setError(e.message || 'Не удалось подписаться в лист ожидания');
    }
  }

  function movieOf(show?: Show | null) {
    return show ? movies.find((m) => m.id === show.movieId) : undefined;
  }

  function hallOf(show?: Show | null) {
    return show ? halls.find((h) => h.id === show.hallId) : undefined;
  }

  function seatClass(seat: Seat) {
    const status = seat.availabilityStatus || 'FREE';
    const selected = selectedSeatId === seat.id;
    return ['seat', seat.seatType?.toLowerCase(), status !== 'FREE' ? 'busy' : 'free', selected ? 'selected' : ''].join(' ');
  }

  function seatForBooking(booking: Booking) {
    return seatsByShowId[booking.showId]?.find((seat) => seat.id === booking.seatId);
  }

  function bookingSeatLabel(booking: Booking) {
    const seat = seatForBooking(booking);
    return seat ? `ряд ${seat.rowNumber}, место ${seat.seatNumber}` : `место #${booking.seatId}`;
  }

  function priceBeforeLoyalty(booking: Booking) {
    return Number(booking.finalPrice || 0) + Number(booking.loyaltyPointsUsed || 0);
  }

  function priceLine(booking: Booking) {
    if (!booking.loyaltyPointsUsed) return `Цена: ${money(booking.finalPrice, booking.currency)}`;
    return `Цена: ${money(priceBeforeLoyalty(booking), booking.currency)} · бонусы: −${booking.loyaltyPointsUsed} ₽ · к оплате: ${money(booking.finalPrice, booking.currency)}`;
  }

  const latestEvents = [...live, ...history].filter(Boolean).slice(0, 8);

  return (
    <div className="app">
      <header className="topbar">
        <div>
          <div className="logo">CS</div>
          <div>
            <h1>CinemaSOP</h1>
            <p>Кинотеатр: фильмы, сеансы, места, бронирование, оплата и события.</p>
          </div>
        </div>
        <nav>
          <button className={page === 'movies' ? 'active' : ''} onClick={() => setPage('movies')}>Афиша</button>
          <button className={page === 'booking' ? 'active' : ''} onClick={() => setPage('booking')}>Бронирование</button>
          <button className={page === 'my-bookings' ? 'active' : ''} onClick={() => setPage('my-bookings')}>Мои брони</button>
          <button className={page === 'events' ? 'active' : ''} onClick={() => setPage('events')}>События</button>
        </nav>
        <div className={`ws ${wsState}`}>
          <span /> WS {wsState === 'connected' ? 'online' : wsState}{wsActiveConnections !== null ? ` · ${wsActiveConnections}` : ''}
        </div>
      </header>

      <main>
        {toast && <div className="notice success" onClick={() => setToast('')}>{toast}</div>}
        {error && <div className="notice error" onClick={() => setError('')}>{error}</div>}
        {loading && <div className="notice">Загрузка данных...</div>}

        <section className="hero">
          <div>
            <p className="eyebrow">Microservice Cinema</p>
            <h2>Покупка билета</h2>
            <p>Выберите фильм, сеанс и место. Финальная цена считается в pricing-service по залу, месту, времени, дню недели и заполняемости.</p>
          </div>
          <div className="hero-card">
            <b>Полный сценарий</b>
            <span>REST → gRPC dynamic pricing/loyalty → RabbitMQ → async enrichment → WebSocket/email</span>
          </div>
        </section>

        <section className="layout">
          <div className="content">
            {page === 'movies' && (
              <section>
                <SectionTitle title="Афиша" subtitle="Фильмы из seed-базы. Нажмите на фильм, чтобы перейти к бронированию." />
                <div className="movie-grid">
                  {movies.map((movie) => (
                    <article className="movie-card" key={movie.id} onClick={() => selectMovie(movie.id, true)}>
                      <div className="poster">{movie.title.slice(0, 2).toUpperCase()}</div>
                      <div>
                        <h3>{movie.title}</h3>
                        <p>{movie.description || 'Описание фильма'}</p>
                        <div className="chips">
                          <span>{movie.genre}</span>
                          <span>{movie.ageRating}</span>
                          <span>{movie.durationMinutes} мин</span>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              </section>
            )}

            {page === 'booking' && (
              <section>
                <SectionTitle title="Бронирование" subtitle="Основной бизнес-сценарий: выбор места, динамическая цена, создание брони, оплата и билет." />
                <div className="booking-grid">
                  <div className="panel">
                    <h3>1. Фильм</h3>
                    <select value={selectedMovieId || ''} onChange={(e) => selectMovie(Number(e.target.value))}>
                      {movies.map((movie) => <option key={movie.id} value={movie.id}>{movie.title}</option>)}
                    </select>
                    {selectedMovie && <p className="muted">{selectedMovie.genre}, {selectedMovie.durationMinutes} мин, {selectedMovie.ageRating}</p>}
                  </div>

                  <div className="panel">
                    <h3>2. Сеанс</h3>
                    <div className="show-list">
                      {!showsForMovie.length && <p className="muted">Для выбранного фильма пока нет доступных сеансов.</p>}
                      {showsForMovie.map((show) => (
                        <button key={show.id} className={selectedShowId === show.id ? 'show selected' : 'show'} onClick={() => selectShow(show.id)}>
                          <b>{fmtDate(show.startTime)}</b>
                          <span>{hallOf(show)?.name || `Зал #${show.hallId}`}</span>
                          <em>{money(show.basePrice, show.currency)}</em>
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="panel wide">
                    <h3>3. Место</h3>
                    <div className="seat-zone">
                      <div className="screen">ЭКРАН</div>
                      <div className="seat-map">
                        {!rows.length && <p className="muted">Выберите сеанс, чтобы загрузить схему зала.</p>}
                        {rows.map(([row, rowSeats]) => (
                          <div className="seat-row" key={row}>
                            <span className="row-label">{row}</span>
                            {rowSeats.map((seat) => (
                              <button
                                key={seat.id}
                                className={seatClass(seat)}
                                title={`Ряд ${seat.rowNumber}, место ${seat.seatNumber}, ${statusRu(seat.availabilityStatus || 'FREE')}`}
                                onClick={() => setSelectedSeatId(seat.id)}
                              >
                                {seat.seatNumber}
                              </button>
                            ))}
                          </div>
                        ))}
                      </div>
                    </div>
                    <div className="legend">
                      <span><i className="free" /> свободно</span>
                      <span><i className="busy" /> занято</span>
                      <span><i className="vip" /> VIP/SOFA</span>
                    </div>
                  </div>

                  <div className="panel wide checkout">
                    <h3>4. Бронь и оплата</h3>
                    <div className="form-grid">
                      <label>
                        Пользователь
                        <select value={customerId} onChange={(e) => { setCustomerId(Number(e.target.value)); setLoyaltyPoints(0); }}>
                          {customers.map((c) => <option key={c.id} value={c.id}>#{c.id} {c.email}</option>)}
                        </select>
                      </label>
                      <label>
                        Бонусы к списанию
                        <input
                          type="number"
                          min="0"
                          value={normalizedLoyaltyPoints}
                          disabled={!selectedCustomer?.registered}
                          onChange={(e) => setLoyaltyPoints(Math.min(Math.max(Number(e.target.value) || 0, 0), availableLoyaltyPoints))}
                        />
                      </label>
                    </div>
                    <div className="loyalty-card">
                      {selectedCustomer?.registered ? (
                        <>
                          <b>Бонусы пользователя</b>
                          <span>Доступно: {availableLoyaltyPoints}</span>
                          <span>К списанию: {normalizedLoyaltyPoints}</span>
                          <span>После списания: {Math.max(availableLoyaltyPoints - normalizedLoyaltyPoints, 0)}</span>
                        </>
                      ) : (
                        <>
                          <b>Бонусы недоступны</b>
                          <span>Guest-пользователь не участвует в системе лояльности.</span>
                        </>
                      )}
                    </div>
                    <div className="summary">
                      <span>Фильм: <b>{selectedMovie?.title || '—'}</b></span>
                      <span>Пользователь: <b>{selectedCustomer?.registered ? `registered · ${availableLoyaltyPoints} бонусов` : 'guest, без бонусов и waitlist'}</b></span>
                      <span>Сеанс: <b>{selectedShow ? fmtDate(selectedShow.startTime) : '—'}</b></span>
                      <span>Место: <b>{selectedSeat ? `${selectedSeat.rowNumber}-${selectedSeat.seatNumber}` : '—'}</b></span>
                      <span>Статус места: <b>{statusRu(selectedSeat?.availabilityStatus || 'FREE')}</b></span>
                      <span>Цена: <b>рассчитывается при создании брони</b></span>
                    </div>
                    <div className="actions">
                      <button className="primary" onClick={createBooking} disabled={!selectedSeat || selectedSeat.availabilityStatus !== 'FREE' || !isBookableShow(selectedShow)}>Создать бронь</button>
                      <button onClick={joinWaitlist} disabled={!selectedSeat || selectedSeat.availabilityStatus === 'FREE' || !selectedCustomer?.registered}>В лист ожидания</button>
                      <button onClick={loadAll}>Обновить</button>
                    </div>
                    {createdBooking && (
                      <div className="result-card">
                        <h4>Бронь #{createdBooking.id}</h4>
                        <p>Статус: <b>{statusRu(createdBooking.status)}</b>. {priceLine(createdBooking)}.</p>
                        {createdBooking.status === 'PENDING_PAYMENT' && <button className="primary" onClick={() => payBooking(createdBooking.id)}>Оплатить</button>}
                      </div>
                    )}
                    {createdTicket && (
                      <div className="ticket">
                        <div className="qr real-qr">
                          <QRCodeSVG value={createdTicket.qrCode || createdTicket.ticketNumber} size={92} />
                        </div>
                        <div>
                          <b>{createdTicket.ticketNumber}</b>
                          <span>Билет #{createdTicket.id}, статус {statusRu(createdTicket.status)}</span>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </section>
            )}

            {page === 'my-bookings' && (
              <section>
                <SectionTitle title="Мои брони" subtitle="Проверка статусов, оплаты, отмены и получения билетов." />
                <div className="table-list">
                  {bookings.slice().sort((a, b) => b.id - a.id).map((booking) => {
                    const show = shows.find((s) => s.id === booking.showId);
                    const movie = movieOf(show);
                    const ticket = tickets.find((t) => t.bookingId === booking.id);
                    return (
                      <article className="booking-card" key={booking.id}>
                        <div>
                          <h3>Бронь #{booking.id} — {movie?.title || `Сеанс #${booking.showId}`}</h3>
                          <p>{fmtDate(show?.startTime)} · {bookingSeatLabel(booking)} · {booking.customerEmail}</p>
                          <div className="chips">
                            <span>{statusRu(booking.status)}</span>
                            <span>{booking.loyaltyPointsUsed ? `к оплате ${money(booking.finalPrice, booking.currency)}` : money(booking.finalPrice, booking.currency)}</span>
                            {booking.loyaltyPointsUsed > 0 && <span>списано {booking.loyaltyPointsUsed} ₽</span>}
                            {ticket && <span>{ticket.ticketNumber}</span>}
                          </div>
                        </div>
                        <div className="card-actions">
                          {booking.status === 'PENDING_PAYMENT' && <button className="primary" onClick={() => payBooking(booking.id)}>Оплатить</button>}
                          {booking.status === 'PENDING_PAYMENT' && <button onClick={() => cancelBooking(booking.id)}>Отменить</button>}
                          {booking.status === 'PAID' && canRefund(show) && <button onClick={() => cancelBooking(booking.id)}>Вернуть билет</button>}
                          {booking.status === 'PAID' && !canRefund(show) && <span className="muted small">Возврат закрыт</span>}
                        </div>
                        {ticket && booking.status === 'PAID' && ticket.status === 'ACTIVE' && (
                          <div className="booking-qr">
                            <QRCodeSVG value={ticket.qrCode || ticket.ticketNumber} size={92} />
                            <span>QR для входа</span>
                          </div>
                        )}
                      </article>
                    );
                  })}
                </div>
              </section>
            )}

            {page === 'events' && (
              <section>
                <SectionTitle title="События" subtitle="Проверка RabbitMQ, audit-service, notification-service и async enrichment." />
                <div className="actions top-actions">
                  <button onClick={loadAll}>Обновить историю</button>
                  <a className="link-button" href={MAILPIT} target="_blank">Mailpit</a>
                  <a className="link-button" href={RABBITMQ} target="_blank">RabbitMQ</a>
                </div>
                <div className="events-grid">
                  <div className="panel">
                    <h3>Audit</h3>
                    <EventList entries={audit.map((e) => ({ title: e.eventType, text: e.description, time: e.processedAt }))} />
                  </div>
                  <div className="panel">
                    <h3>Notifications</h3>
                    <EventList entries={history.map((e) => ({ title: e.title, text: e.message, time: e.receivedAt }))} />
                  </div>
                </div>
              </section>
            )}
          </div>

          <aside className="sidebar">
            <div className="panel sticky">
              <h3>Realtime WS</h3>
              <div className={`connection ${wsState}`}>{wsState === 'connected' ? 'connected' : 'offline'}{wsActiveConnections !== null ? ` · active ${wsActiveConnections}` : ''}</div>
              <EventList entries={latestEvents.map((e) => ({ title: e.title || e.eventType || 'event', text: e.message || e.eventType || '', time: e.receivedAt }))} />
            </div>
          </aside>
        </section>
      </main>
    </div>
  );
}

function SectionTitle({ title, subtitle }: { title: string; subtitle: string }) {
  return (
    <div className="section-title">
      <h2>{title}</h2>
      <p>{subtitle}</p>
    </div>
  );
}

function EventList({ entries }: { entries: { title: string; text: string; time?: string }[] }) {
  if (!entries.length) return <p className="muted">Пока событий нет. Создайте или оплатите бронь.</p>;
  return (
    <div className="event-list">
      {entries.slice(0, 12).map((entry, index) => (
        <div className="event-item" key={`${entry.title}-${index}`}>
          <b>{entry.title}</b>
          <p>{entry.text}</p>
          <span>{fmtDate(entry.time)}</span>
        </div>
      ))}
    </div>
  );
}
