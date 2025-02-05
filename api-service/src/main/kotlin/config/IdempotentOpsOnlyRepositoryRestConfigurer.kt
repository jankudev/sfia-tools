package dev.janku.sfia.apiservice.config

import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry

@Component
class IdempotentOpsOnlyRepositoryRestConfigurer : RepositoryRestConfigurer {

  companion object {
    private val DISABLE_HTTP_METHODS = arrayOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE)
  }

  @Override
  override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration, cors: CorsRegistry) {
    val exposureConfiguration = config.getExposureConfiguration()
    exposureConfiguration
      .withItemExposure { metadata, httpMethods ->
        httpMethods.disable(*DISABLE_HTTP_METHODS)
      }
      .withCollectionExposure { metadata, httpMethods ->
        httpMethods.disable(*DISABLE_HTTP_METHODS)
      }
  }
}