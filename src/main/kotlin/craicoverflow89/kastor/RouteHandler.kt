package craicoverflow89.kastor

abstract class RouteHandler(val pattern: String)
{
    private var proxy: KastorServer.RouteHandlerProxy? = null

    abstract fun invoke(method: String, parameter: HashMap<String, Any>)

    fun injectProxy(proxyInject: KastorServer.RouteHandlerProxy)
    {
        proxy = proxyInject
    }

    fun redirect(path: String)
    {
        proxy!!.redirect(path)
    }

    fun render(content: String, status: Int = 200)
    {
        proxy!!.render(content, status)
    }

}