package com.dts.practice.integration;

import java.nio.file.*;
import java.sql.*;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/** Strictly owned test DB only. A business database URL is refused, even when supplied. */
final class SimulationSourceTestDatabase implements AutoCloseable {
    final DataSource source;final TransactionTemplate transaction;private GenericContainer<?> container;
    SimulationSourceTestDatabase() throws Exception {
        String url=System.getenv("DTS_SIMULATION_INTEGRATION_TEST_JDBC");String user="test",password="test";
        if(url!=null){
            if(!url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]{1,5}/dts_integration_test_[a-z0-9_]+"))throw new IllegalArgumentException("Only an isolated test database is permitted");
            user=System.getenv().getOrDefault("DTS_SIMULATION_INTEGRATION_TEST_USER","dts_test");password=System.getenv().getOrDefault("DTS_SIMULATION_INTEGRATION_TEST_PASSWORD","");
            String child="dts_integration_test_"+java.util.UUID.randomUUID().toString().replace("-","");
            try(var c=DriverManager.getConnection(url,user,password)){
                if(!c.getCatalog().startsWith("dts_integration_test_"))throw new IllegalStateException("Not a test database");
                try(var statement=c.createStatement()){statement.execute("CREATE DATABASE "+child);}
            }
            url=url.substring(0,url.lastIndexOf('/')+1)+child;
        }else{
            container=new GenericContainer<>("postgres:16-alpine").withEnv("POSTGRES_DB","dts_integration_test_simulation").withEnv("POSTGRES_USER",user).withEnv("POSTGRES_PASSWORD",password)
                    .withExposedPorts(5432).waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n",2));
            container.start();url="jdbc:postgresql://"+container.getHost()+":"+container.getMappedPort(5432)+"/dts_integration_test_simulation";
        }
        source=new DriverManagerDataSource(url,user,password);transaction=new TransactionTemplate(new DataSourceTransactionManager(source));
        org.flywaydb.core.Flyway.configure().dataSource(source).locations("classpath:db/migration").target("34").ignoreMigrationPatterns("*:pending").load().migrate();
    }
    public void close(){if(container!=null)container.stop();}
}
