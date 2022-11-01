package com.example.audiorecorderandimagepicker.data.network.common

import java.io.IOException

open class ApiException(message: String) : IOException(message)

class NoInternetException(message: String) : ApiException(message)