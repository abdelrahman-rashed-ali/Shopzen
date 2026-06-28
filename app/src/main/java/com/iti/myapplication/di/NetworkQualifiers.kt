package com.iti.myapplication.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RestClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GraphQLClient