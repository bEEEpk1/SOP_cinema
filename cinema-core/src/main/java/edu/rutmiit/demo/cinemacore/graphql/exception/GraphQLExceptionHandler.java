package edu.rutmiit.demo.cinemacore.graphql.exception;

import com.netflix.graphql.types.errors.TypedGraphQLError;
import edu.rutmiit.demo.cinemaapicontract.exception.BookingStateConflictException;
import edu.rutmiit.demo.cinemaapicontract.exception.CustomerAlreadyExistsException;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemaapicontract.exception.SeatAlreadyReservedException;
import edu.rutmiit.demo.cinemacore.exception.ApiConflictException;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters params) {
        Throwable exception = params.getException();

        if (exception instanceof ResourceNotFoundException) {
            return completed(TypedGraphQLError.newNotFoundBuilder()
                    .message(exception.getMessage())
                    .path(params.getPath())
                    .build());
        }

        if (exception instanceof SeatAlreadyReservedException
                || exception instanceof BookingStateConflictException
                || exception instanceof CustomerAlreadyExistsException
                || exception instanceof ApiConflictException) {
            return completed(TypedGraphQLError.newConflictBuilder()
                    .message(exception.getMessage())
                    .path(params.getPath())
                    .build());
        }

        if (exception instanceof IllegalArgumentException) {
            return completed(TypedGraphQLError.newBadRequestBuilder()
                    .message(exception.getMessage())
                    .path(params.getPath())
                    .build());
        }

        return completed(TypedGraphQLError.newInternalErrorBuilder()
                .message("Внутренняя ошибка сервера")
                .path(params.getPath())
                .build());
    }

    private CompletableFuture<DataFetcherExceptionHandlerResult> completed(graphql.GraphQLError error) {
        return CompletableFuture.completedFuture(
                DataFetcherExceptionHandlerResult.newResult()
                        .error(error)
                        .build()
        );
    }
}
