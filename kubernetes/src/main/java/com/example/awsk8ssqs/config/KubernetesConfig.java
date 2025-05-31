package com.example.awsk8ssqs.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class KubernetesConfig {

    @Value("${kubernetes.config.in-cluster:false}")
    private boolean inCluster;

    @Value("${kubernetes.config.kubeconfig-path:#{null}}")
    private String kubeconfigPath;

    @Bean
    public ApiClient kubernetesApiClient() throws IOException {
        ApiClient client;
        
        if (inCluster) {
            log.info("Configuring Kubernetes client for in-cluster access");
            client = Config.fromCluster();
        } else if (kubeconfigPath != null) {
            log.info("Configuring Kubernetes client from kubeconfig: {}", kubeconfigPath);
            client = Config.fromConfig(kubeconfigPath);
        } else {
            log.info("Configuring Kubernetes client from default kubeconfig");
            client = Config.defaultClient();
        }
        
        // Configure client
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        
        return client;
    }

    @Bean
    public CustomObjectsApi customObjectsApi(ApiClient apiClient) {
        return new CustomObjectsApi(apiClient);
    }

    /**
     * Generic Kubernetes API for OpenSearchCluster custom resources
     * This provides type-safe access to our custom resources
     */
    @Bean
    public GenericKubernetesApi<Object, Object> openSearchClusterApi(ApiClient apiClient) {
        return new GenericKubernetesApi<>(
            Object.class,  // We'll use Object and cast to our types
            Object.class,  // For list operations
            "opensearch.aws.com",    // API group
            "v1",                    // API version  
            "opensearchclusters",    // Resource plural name
            apiClient
        );
    }
} 