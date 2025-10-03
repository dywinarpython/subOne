FROM quay.io/keycloak/keycloak:latest

COPY keycloak-kafka-1.3.0-jar-with-dependencies.jar /opt/keycloak/providers/

RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
