package com.nhn.pinpoint.profiler.modifier.orm.ibatis;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.Method;
import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.orm.ibatis.interceptor.IbatisSqlMapOperationInterceptor;
import com.nhn.pinpoint.profiler.modifier.orm.ibatis.interceptor.IbatisScope;
import com.nhn.pinpoint.profiler.util.DepthScope;

/**
 * Base class for modifying iBatis client classes
 *  
 * @author Hyun Jeong
 */
public abstract class IbatisClientModifier extends AbstractModifier {

	private static final ServiceType serviceType = ServiceType.IBATIS;
	private static final DepthScope scope = IbatisScope.SCOPE;

    protected Logger logger;

    protected abstract MethodFilter getIbatisApiMethodFilter();

	public IbatisClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
		}
		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass ibatisClientImpl = byteCodeInstrumentor.getClass(javassistClassName);
			List<Method> declaredMethods = ibatisClientImpl.getDeclaredMethods(getIbatisApiMethodFilter());

			for (Method method : declaredMethods) {
				Interceptor ibatisApiInterceptor = new IbatisSqlMapOperationInterceptor(serviceType);
				ibatisClientImpl.addScopeInterceptor(method.getMethodName(), method.getMethodParams(), ibatisApiInterceptor, scope);
			}
			
			return ibatisClientImpl.toBytecode();
		} catch (Throwable e) {
			this.logger.warn("{} modifier error. Cause:{}", javassistClassName, e.getMessage(), e);
			return null;
		}
	}
}