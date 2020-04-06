package com.coroutinesflow.frameworks.network

import com.coroutinesflow.AppExceptions
import com.coroutinesflow.base.data.APIState
import com.coroutinesflow.base.data.BaseModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import retrofit2.Response

@ExperimentalCoroutinesApi
suspend fun <RESPONSE : BaseModel<RESPONSE>> getRemoteDate(
    iODispatcher: CoroutineDispatcher = Dispatchers.IO,
    function: suspend NetworkHandler<RESPONSE>. () -> Response<RESPONSE>
): Flow<APIState<RESPONSE>> = NetworkHandler<RESPONSE>().getRemoteDataAPI(function, iODispatcher)


class NetworkHandler<RESPONSE : Any> {

    @ExperimentalCoroutinesApi
    suspend fun getRemoteDataAPI(
        function: suspend NetworkHandler<RESPONSE>.() -> Response<RESPONSE>,
        iODispatcher: CoroutineDispatcher
    ): Flow<APIState<RESPONSE>> =
        flow {
            runCatching {
                function.invoke(this@NetworkHandler)
            }.onSuccess {
                if (it.isSuccessful && it.body() is RESPONSE) {
                    emit(getDataOrThrowException(it))
                } else {
                    emit(throwException(it))
                }
            }.onFailure {
                emit(APIState.ErrorState(it))
            }
        }.onStart {
            emit(APIState.LoadingState)
        }.catch {
            emit(APIState.ErrorState(AppExceptions.GenericErrorException))
        }.flowOn(iODispatcher)


    private fun throwException(it: Response<RESPONSE>): APIState<RESPONSE> {
        return it.errorBody()?.let { responseBody ->
            if (responseBody.toString().isEmpty()) {
                APIState.ErrorState(AppExceptions.HttpException)
            } else {
                APIState.ErrorState(AppExceptions.GenericErrorException)
            }
        } ?: APIState.ErrorState(AppExceptions.GenericErrorException)
    }

    private fun getDataOrThrowException(it: Response<RESPONSE>): APIState<RESPONSE> {

        return it.body()?.let {
            if (it.toString().isEmpty()) {
                APIState.ErrorState(AppExceptions.HttpException)
            } else {
                APIState.DataStat(it)
            }
        } ?: APIState.ErrorState(AppExceptions.HttpException)
    }
}