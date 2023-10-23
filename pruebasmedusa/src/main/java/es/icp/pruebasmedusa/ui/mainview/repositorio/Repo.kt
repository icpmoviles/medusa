package es.icp.pruebasmedusa.ui.mainview.repositorio

import es.icp.medusa.data.remote.modelos.AuthRequest
import es.icp.pruebasmedusa.ui.mainview.service.MockService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repo @Inject constructor(
    private val api: MockService
){
    suspend fun getTokenPerseo( request: AuthRequest) = api.getTokenPerseo( request)
}