package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.frame.ioc.aop.*
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.common.KotlinClassUtil
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.common.ProxyUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap

/**
 * so far,only this context use proxy
 * @author Dandelion
 */
open class IocContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(IocContext::class)
        internal val iocConfigurationBean = IocConfigurationBean()
        internal val iocBeanMap = ConcurrentHashMap<String, IocBean>()
    }

    /**
     * initialize
     * @param parameters
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val path = this.classesRealPath + fixParameters
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            //configuration
            val configurationElementList = root.getElementsByTagName(IocConfigurationBean.TAG_CONFIGURATION)
            if (configurationElementList != null && configurationElementList.length > 0) {
                val configurationAttributeMap = configurationElementList.item(0).attributes
                JavaXmlUtil.initializeFromAttributeMap(iocConfigurationBean, configurationAttributeMap)
            }
            //ioc bean
            val beanElementList = root.getElementsByTagName(IocBean.TAG_BEAN) ?: return
            //xml to object
            val beanElementLength = beanElementList.length
            for (index in 0 until beanElementLength) {
                val beanElement = beanElementList.item(index)
                //bean
                val iocBean = IocBean()
                val attributeMap = beanElement.attributes
                JavaXmlUtil.initializeFromAttributeMap(iocBean, attributeMap)
                //constructor
                val childNodeList = beanElement.childNodes ?: continue
                val childNodeLength = childNodeList.length
                for (childNodeIndex in 0 until childNodeLength) {
                    val childNode = childNodeList.item(childNodeIndex)
                    val nodeName = childNode.nodeName
                    when (nodeName) {
                        IocConstructorBean.TAG_CONSTRUCTOR -> {
                            val iocConstructorBean = IocConstructorBean()
                            val iocConstructorAttributeMap = childNode.attributes
                            JavaXmlUtil.initializeFromAttributeMap(iocConstructorBean, iocConstructorAttributeMap)
                            iocBean.iocConstructorBean = iocConstructorBean
                        }
                        IocPropertyBean.TAG_PROPERTY -> {
                            val iocPropertyBean = IocPropertyBean()
                            val iocPropertyAttributeMap = childNode.attributes
                            JavaXmlUtil.initializeFromAttributeMap(iocPropertyBean, iocPropertyAttributeMap)
                            iocBean.addIocPropertyBean(iocPropertyBean)
                        }
                        IocAfterInjectBean.TAG_AFTER_INJECT -> {
                            val iocAfterInjectBean = IocAfterInjectBean()
                            val iocAfterInjectAttributeMap = childNode.attributes
                            JavaXmlUtil.initializeFromAttributeMap(iocAfterInjectBean, iocAfterInjectAttributeMap)
                            iocBean.addIocAfterInjectBean(iocAfterInjectBean)
                        }//after inject
                        //property
                    }//after inject
                    //property
                }
                if (!iocBeanMap.containsKey(iocBean.id)) {
                    iocBeanMap[iocBean.id] = iocBean
                } else {
                    logger.error("ioc context initialize error, duplicate ioc bean id:%s", iocBean.id)
                }
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    /**
     * destroy
     */
    override fun destroy() {
        iocBeanMap.clear()
    }

    /**
     * ioc bean object instantiated
     * @throws Exception
     */
    @Throws(Exception::class)
    open fun instantiateIocBeanObject() {
        iocBeanMap.forEach { (_, iocBean) ->
            try {
                if (iocBean.iocConstructorBean != null) {
                    instantiateIocBeanObjectByConstructor(iocBean)
                } else {
                    instantiateIocBeanObjectByDefaultConstructor(iocBean)
                }
            } catch (e: Throwable) {
                logger.error("instantiate ioc bean object error, id:${iocBean.id}, type:${iocBean.type}, value:${iocBean.value}", e)
                throw e
            }
        }
    }

    /**
     * instantiated one ioc bean by constructor
     * @param iocBean
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun instantiateIocBeanObjectByConstructor(iocBean: IocBean) {
        val iocConstructorBean = iocBean.iocConstructorBean
        val type = iocBean.type
        val beanClass = iocBean.beanClass
        val constructorTypes = iocConstructorBean!!.types
        val constructorReferences = iocConstructorBean.references
        val constructorTypeArray = constructorTypes.split(Constants.Symbol.COMMA)
        val constructorReferenceArray = constructorReferences.split(Constants.Symbol.COMMA)
        val constructorTypeClassArray = arrayOfNulls<Class<*>>(constructorTypeArray.size)
        val constructorReferenceObjectArray = arrayOfNulls<Any>(constructorReferenceArray.size)
        var index = 0
        for (constructorType in constructorTypeArray) {
            constructorTypeClassArray[index++] = KotlinClassUtil.getClass(this.classLoader, constructorType)?.java
        }
        index = 0
        for (constructorReference in constructorReferenceArray) {
            val referenceObject = iocBeanMap[constructorReference]
            if (referenceObject != null) {
                var referenceProxyObject = referenceObject.proxyInstance
                if (referenceProxyObject == null) {
                    instantiateIocBeanObjectByDefaultConstructor(referenceObject)
                    referenceProxyObject = referenceObject.proxyInstance
                }
                constructorReferenceObjectArray[index++] = referenceProxyObject
            } else {
                constructorReferenceObjectArray[index++] = null
            }
        }
        val constructor: Constructor<*> = if (beanClass == null) {
            this.classLoader.loadClass(type).getConstructor(*constructorTypeClassArray)
        } else {
            beanClass.getConstructor(*constructorTypeClassArray)
        }
        val beanInstance = constructor.newInstance(*constructorReferenceObjectArray)
        iocBean.beanInstance = beanInstance
        if (iocBean.proxy) {
            val classLoader = if (beanClass == null) {
                this.classLoader
            } else {
                beanClass.classLoader
            }
            val proxyInstance = ProxyUtil.newProxyInstance(classLoader, beanInstance, AopInvocationHandler<Any>(beanInstance))
            iocBean.proxyInstance = proxyInstance
        } else {
            iocBean.proxyInstance = beanInstance
        }
    }

    /**
     * instantiated one ioc bean by default constructor
     * @param iocBean
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun instantiateIocBeanObjectByDefaultConstructor(iocBean: IocBean) {
        val type = iocBean.type
        val beanClass = iocBean.beanClass
        val value = iocBean.value
        var beanInstance = iocBean.beanInstance
        if (beanInstance == null) {
            val iocBeanId = iocBean.id
            if (objectMap.containsKey(iocBeanId)) {//object map contain,prove duplicate config in ioc,copy to ioc bean
                logger.warning("object map contains id:%s", iocBeanId)
                beanInstance = objectMap[iocBeanId]
                iocBean.beanInstance = beanInstance
                iocBean.proxyInstance = beanInstance
            } else {//normal config
                if (KotlinClassUtil.isSimpleClass(type)) {
                    val kClass = KotlinClassUtil.getClass(this.classLoader, type)!!
                    beanInstance = KotlinClassUtil.changeType(kClass, arrayOf(value))
                } else {
                    beanInstance = if (beanClass == null) {
                        this.classLoader.loadClass(type).newInstance()
                    } else {
                        beanClass.newInstance()
                    }
                    //aop interceptor
                    if (beanInstance is BeforeInvokeProcessor) {
                        AopInvocationHandler.addBeforeInvokeProcessor(beanInstance)
                    }
                    if (beanInstance is AfterReturningProcessor) {
                        AopInvocationHandler.addAfterReturningProcessor(beanInstance)
                    }
                    if (beanInstance is AfterThrowingProcessor) {
                        AopInvocationHandler.addAfterThrowingProcessor(beanInstance)
                    }
                    if (beanInstance is InvokeProcessor) {
                        AopInvocationHandler.setInvokeProcessor(beanInstance)
                    }
                }
                iocBean.beanInstance = beanInstance!!
                if (iocBean.proxy) {
                    val classLoader = if (beanClass == null) {
                        this.classLoader
                    } else {
                        beanClass.classLoader
                    }
                    val proxyInstance = ProxyUtil.newProxyInstance(classLoader, beanInstance, AopInvocationHandler(beanInstance))
                    iocBean.proxyInstance = proxyInstance
                } else {
                    iocBean.proxyInstance = beanInstance
                }
            }
            logger.info("Instantiating, " + iocBean.type + "<->id:" + iocBeanId + "<->proxy:" + iocBean.proxy + "<->proxyInstance:" + iocBean.proxyInstance + "<->instance:" + iocBean.beanInstance)
        }
    }

    /**
     * inject,only this injection put the proxy instance to object map
     * @throws Exception
     */
    @Throws(Exception::class)
    fun inject() {
        //instantiate all ioc bean
        instantiateIocBeanObject()
        //inject
        val objectInjectType = iocConfigurationBean.objectInjectType
        if (objectInjectType == IocConfigurationBean.INJECT_TYPE_AUTO_BY_ID) {
            objectMap.forEach { (id, instance) ->
                this.autoInjectObjectById(id, instance)
            }
        } else if (objectInjectType == IocConfigurationBean.INJECT_TYPE_AUTO_BY_TYPE) {
            objectMap.forEach { (id, instance) ->
                this.autoInjectObjectByType(id, instance)
            }
        }
        iocBeanMap.forEach { (_, iocBean) ->
            val injectType = iocBean.injectType
            val iocBeanId = iocBean.id
            val beanInstance = iocBean.beanInstance!!
            when (injectType) {
                IocBean.INJECT_TYPE_AUTO_BY_ID -> this.autoInjectObjectById(iocBeanId, beanInstance)
                IocBean.INJECT_TYPE_AUTO_BY_TYPE -> this.autoInjectObjectByType(iocBeanId, beanInstance)
                IocBean.INJECT_TYPE_MANUAL -> this.manualInject(iocBean)
            }
            if (!objectMap.containsKey(iocBeanId)) {
                objectMap[iocBeanId] = iocBean.proxyInstance!!
            } else {
                logger.warning("inject, object map contains the ioc bean id:%s", iocBeanId)
            }
        }
    }

    /**
     * auto inject instance by type
     * @throws Exception
     */
    @Throws(Exception::class)
    fun autoInjectObjectByType(id: String, instance: Any) {
        val objectMethods = instance.javaClass.methods
        for (method in objectMethods) {
            val methodName = method.name
            if (!methodName.startsWith(Constants.Method.PREFIX_SET)) {
                continue
            }
            val types = method.parameterTypes
            if (types == null || types.size != 1) {
                continue
            }
            val parameterClass = types[0]
            val parameterClassName = parameterClass.name
            iocBeanMap.forEach { (_, iocBean) ->
                val beanInstance = iocBean.beanInstance
                val proxyInstance = iocBean.proxyInstance
                val beanInstanceClassName = beanInstance!!.javaClass.name
                if (parameterClassName == beanInstanceClassName) {
                    logger.info("Auto injecting by type, instance id:%s, %s <- %s", id, instance.javaClass.name, beanInstance.javaClass.name)
                    method.invoke(instance, proxyInstance)
                } else {
                    val interfaces = beanInstance.javaClass.interfaces
                    if (interfaces != null) {
                        for (interfaceClass in interfaces) {
                            val beanInstanceClassInterfaceName = interfaceClass.name
                            if (parameterClassName == beanInstanceClassInterfaceName) {
                                logger.info("Auto injecting by type, instance id:%s, %s <- %s", id, instance.javaClass.name, beanInstance.javaClass.name)
                                method.invoke(instance, proxyInstance)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * auto inject object by id
     * @throws Exception
     */
    @Throws(Exception::class)
    fun autoInjectObjectById(id: String, instance: Any) {
        val objectMethods = instance.javaClass.methods
        for (method in objectMethods) {
            val methodName = method.name
            if (!methodName.startsWith(Constants.Method.PREFIX_SET)) {
                continue
            }
            val types = method.parameterTypes
            if (types == null || types.size != 1) {
                continue
            }
            val fieldName = ObjectUtil.methodNameToFieldName(Constants.Method.PREFIX_SET, methodName)
            val instanceClassName = instance.javaClass.name
            val referenceIocBean = iocBeanMap[fieldName]
            if (referenceIocBean == null) {
                logger.warning("Auto injecting by id error, can not find the reference instance, instance id:%s, instance class name:%s, field name:%s", id, instanceClassName, fieldName)
                continue
            }
            val proxyInstance = referenceIocBean.proxyInstance
            logger.info("Auto injecting by id, instance id:%s, reference instance id:%s, method name:%s, %s <- %s", id, fieldName, methodName, instanceClassName, referenceIocBean.type)
            try {
                method.invoke(instance, proxyInstance)
            } catch (e: Throwable) {
                logger.error("Auto injecting by id error, instance id:%s, instance class name:%s, field name:%s, reference type:%s, real type:%s", e, id, instanceClassName, fieldName, types[0], referenceIocBean.type)
                throw e
            }
        }
    }

    /**
     * manual inject,must config all bean in ioc
     * @throws Exception
     */
    @Throws(Exception::class)
    fun manualInject(iocBean: IocBean) {
        val iocBeanId = iocBean.id
        val iocPropertyBeanList = iocBean.iocPropertyBeanList
        logger.info("Manual injecting, instance id:%s, instance class name:%s", iocBeanId, iocBean.type)
        for (iocPropertyBean in iocPropertyBeanList) {
            val propertyName = iocPropertyBean.name
            val referenceBeanId = iocPropertyBean.reference
            if (!iocBeanMap.containsKey(referenceBeanId)) {
                logger.error("Manual injecting error, can not find the reference instance id, instance class id:%s, instance class name:%s, field name:%s, reference id:%s", iocBeanId, iocBean.type, propertyName, referenceBeanId)
                continue
            }
            val instance = iocBean.beanInstance
            val objectMethods = instance!!.javaClass.methods
            for (method in objectMethods) {
                val methodName = method.name
                if (!methodName.startsWith(Constants.Method.PREFIX_SET)) {
                    continue
                }
                val fieldName = ObjectUtil.methodNameToFieldName(Constants.Method.PREFIX_SET, methodName)
                if (propertyName != fieldName) {
                    continue
                }
                val types = method.parameterTypes
                if (types == null || types.size != 1) {
                    continue
                }
                val referenceIocBean = iocBeanMap[referenceBeanId]
                if (referenceIocBean == null) {
                    logger.warning("Manual injecting by id error, can not find the reference instance, instance id:%s, field name:%s, reference bean id:%s", iocBeanId, fieldName, referenceBeanId)
                    continue
                }
                val instanceClassName = instance.javaClass.name
                val proxyInstance = referenceIocBean.proxyInstance
                logger.info("Manual injecting, instance id:%s, %s <- %s", iocBeanId, iocBean.type, referenceIocBean.type)
                try {
                    method.invoke(instance, proxyInstance)
                } catch (e: Throwable) {
                    logger.error("Manual injecting error, instance id:%s, instance class name:%s, field name:%s, reference type:%s, real type:%s", e, iocBeanId, instanceClassName, fieldName, types[0], referenceIocBean.type)
                    throw e
                }
            }
        }
    }

    /**
     * after inject
     * @throws Exception
     */
    @Throws(Exception::class)
    fun afterInject() {
        iocBeanMap.forEach { (id, iocBean) ->
            val iocAfterInjectBeanList = iocBean.iocAfterInjectBeanList
            for (iocAfterInjectBean in iocAfterInjectBeanList) {
                val proxyInstance = iocBean.proxyInstance
                if (proxyInstance == null) {
                    logger.error("After inject, proxy instance is null, instance id:%s", id)
                    continue
                }
                val method = proxyInstance.javaClass.getMethod(iocAfterInjectBean.method)
                logger.info("After inject, instance id:%s, proxyInstance:%s, method:%s", id, proxyInstance, iocAfterInjectBean.method)
                try {
                    method.invoke(proxyInstance)
                } catch (e: Throwable) {
                    logger.error("After inject error, instance id:%s, proxyInstance:%s, method:%s", e, id, proxyInstance, iocAfterInjectBean.method)
                    //no need to throw exception, it will break the main thread
//                    throw e
                }
            }
        }
    }

    /**
     * put to ioc bean map
     * @param iocBean
     */
    fun putToIocBeanMap(iocBean: IocBean) {
        if (iocBean.id.isBlank()) {
            error("ioc bean id can not be blank")
        }
        iocBeanMap[iocBean.id] = iocBean
    }
}
