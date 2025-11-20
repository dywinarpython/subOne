import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: "http://keycloak:8080/",  
    realm: "subOne",
    clientId: "subone_react"
});

export default keycloak;
