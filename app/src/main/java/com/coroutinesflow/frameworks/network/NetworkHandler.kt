package com.coroutinesflow.frameworks.network

import com.coroutinesflow.AppExceptions
import com.coroutinesflow.base.data.APIState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class NetworkHandler<RESPONSE : Any> {

    private val timeOutInMillisSecond = 10000L
    private val apisJobsHashMap = HashMap<String, Job>()
    private lateinit var state: APIState<RESPONSE>
    private lateinit var response: Response<RESPONSE>


    @ExperimentalCoroutinesApi
    private suspend fun getRemoteDataAPI(
        apiID: String,
        function: suspend NetworkHandler<RESPONSE>.() -> Response<RESPONSE>,
        iODispatcher: CoroutineDispatcher
    ): Flow<APIState<RESPONSE>> =
        flow {
            runCatching {
                CoroutineScope(iODispatcher).launch {
                    withTimeout(timeOutInMillisSecond) {
                        withContext(iODispatcher) {
                            response = function.invoke(this@NetworkHandler)
                        }
                    }
                }
            }.onSuccess { job: Job ->
                apisJobsHashMap[apiID] = job
                job.join()
                job.invokeOnCompletion {
                    state = it?.let { notNullThrowable ->
                        APIState.ErrorState(notNullThrowable)
                    } ?: getDataOrThrowException(response)
                }
                emit(state)

            }.onFailure {
                emit(APIState.ErrorState(it))
            }
        }.catch {
            emit(APIState.ErrorState(AppExceptions.GenericErrorException))
        }.flowOn(iODispatcher)


    private fun getDataOrThrowException(it: Response<RESPONSE>) =
        it.body()?.let {
            if (it.toString().isEmpty()) {
                APIState.ErrorState(AppExceptions.HttpException)
            } else {
                APIState.DataStat(it)
            }
        } ?: APIState.ErrorState(AppExceptions.HttpException)


    @ExperimentalCoroutinesApi
    suspend fun getRemoteDate(
        apiID: String,
        iODispatcher: CoroutineDispatcher,
        function: suspend NetworkHandler<RESPONSE>. () -> Response<RESPONSE>
    ): Flow<APIState<RESPONSE>> =
        NetworkHandler<RESPONSE>().getRemoteDataAPI(apiID, function, iODispatcher)

    fun cancelJob(apiID: String): Boolean {
        if (apisJobsHashMap.size > 0 && apisJobsHashMap.containsKey(apiID))
            apisJobsHashMap.getValue(apiID).cancel()
        return apisJobsHashMap.getValue(apiID).isCancelled
    }
}

