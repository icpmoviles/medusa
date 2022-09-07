package es.icp.medusa.repo.interfaces

interface Ws_Callback {
    fun online (response: Any)
    fun offline()
}