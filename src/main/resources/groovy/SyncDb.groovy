package groovy

import com.aopbuddy.record.CaffeineCache

return CaffeineCache.getCache().asMap().findAll { key, value -> key.getMethodChainId() > params.methodChainId }
