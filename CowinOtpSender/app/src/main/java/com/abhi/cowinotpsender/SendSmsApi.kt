package com.abhi.cowinotpsender

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface SendSmsApi {

    @GET
    fun sendOtpUrl(@Url url: String): Observable<Response<Any>>
}