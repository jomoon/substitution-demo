package com.hjj.substitutiondemo.controller;

import com.google.common.collect.Lists;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.LoadBalancerBuilder;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import rx.Observable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/substitution")
public class SubstitutionController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/callHello")
    public String callHello() {
        return restTemplate.getForObject("http://HJJ-HOUSE/house/hello",String.class);
    }

    /**
     * 通过ribbon来进行负载均衡 但是客户端需要知道服务的地址这个是一个问题，之后结合eureka 就可以了  通过注册中心获取这个服务的地址
     * @return
     */
    @GetMapping("/ribbon")
    public String call() {
        List<Server> serverList = Lists.newArrayList(
                                        new Server("localhost",8081),
                                        new Server("localhost",8083));
        BaseLoadBalancer loadBalancer =
                LoadBalancerBuilder.newBuilder().buildFixedServerListLoadBalancer(serverList);

        String get = LoadBalancerCommand.<String>builder()
                .withLoadBalancer(loadBalancer)
                .build()
                .submit(new ServerOperation<String>() {
                    @Override
                    public Observable<String> call(Server server) {
                        try {
                            String addr = "http://" + server.getHost() + ":" + server.getPort() + "/house/hello";
                            System.err.println(addr);
                            URL url = new URL(addr);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.connect();
                            InputStream in = connection.getInputStream();
                            //
                            StringBuffer stringBuffer = new StringBuffer();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                            boolean firstLine = true;
                            String line = null; ;
                            while((line = bufferedReader.readLine()) != null) {
                                if (!firstLine) {
                                    stringBuffer.append(System.getProperty("line.separator"));
                                } else {
                                    firstLine = false;
                                }
                                stringBuffer.append(line);
                            }
                            return Observable.just(new String(stringBuffer));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Observable.error(e);
                        }
                    }
                }).toBlocking().first();
        return get;

    }
 }
