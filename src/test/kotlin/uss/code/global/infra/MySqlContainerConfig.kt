package uss.code.global.infra

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.mysql.MySQLContainer

@TestConfiguration(proxyBeanMethods = false)
class MySqlContainerConfig {
    @Bean
    fun mysqlContainer(): MySQLContainer {
        return MySQLContainer(MYSQL_IMAGE)
    }

    /**
     * DataSourceConfig가 spring.datasource 프로퍼티로 DataSource를 직접 만들어 @ServiceConnection이 끼어들지 못한다.
     * 컨테이너 접속 정보를 같은 프로퍼티에 직접 주입한다.
     */
    @Bean
    fun mysqlPropertyRegistrar(mysqlContainer: MySQLContainer): DynamicPropertyRegistrar {
        return DynamicPropertyRegistrar { registry ->
            registry.add("spring.datasource.url") { mysqlContainer.jdbcUrl }
            registry.add("spring.datasource.username") { mysqlContainer.username }
            registry.add("spring.datasource.password") { mysqlContainer.password }
            registry.add("spring.datasource.driver-class-name") { mysqlContainer.driverClassName }
        }
    }

    companion object {
        private const val MYSQL_IMAGE = "mysql:8.0"
    }
}
