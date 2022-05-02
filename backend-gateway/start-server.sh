nohup java -javaagent:/opt/software/skywalking/agent/skywalking-agent.jar \
-Dskywalking.agent.service_name=backendservice-gateway \
-Dskywalking.collector.backend_service=127.0.0.1:11800 \
-jar backend-gateway-1.0-boot.jar \
--server.port=9991 \
2>&1 > ./nohup.log &