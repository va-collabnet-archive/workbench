java \
    -ea \
    -Xdebug \
    -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n \
    -Xms1400m \
    -Xmx1400m \
    -Djava.util.logging.config.file=config/logging.properties \
    -Djava.security.policy=config/dwa.policy \
    -Djava.security.properties=config/dynamic-policy.security-properties \
    -Djava.security.auth.login.config=config/dwa.login \
    -Djavax.net.ssl.trustStore=config/prebuiltkeys/truststore \
    -Djava.protocol.handler.pkgs=net.jini.url \
    -Dorg.dwfa.jiniport=8080 \
    -cp \
        lib/browser-2.1.jar:lib/colt-1.2.0.jar:lib/commons-collections-3.1.jar:lib/commons-primitives-1.0.jar:lib/concurrent-1.3.4.jar:lib/geronimo-activation_1.1_spec-1.0.2.jar:lib/geronimo-javamail_1.4_mail-1.6.jar:lib/ihtsdo-resources-1.0.jar:lib/je-4.0.103.jar:lib/jgraphx-examples-1.4.0.0-1.jar:lib/jna-3.0.5.jar:lib/jsk-lib-2.1.jar:lib/jsk-platform-2.1.jar:lib/jung-1.7.6.jar:lib/lucene-core-2.9.2.jar:lib/mahalo-2.1.jar:lib/snorocket-core-1.1.14.jar:lib/snorocket-snapi-1.1.14.jar:lib/start-2.1.jar:lib/svnkit-1.3.3.jar:lib/svnkit-javahl-1.2.1.5297.jar:lib/trilead-ssh2-build213-svnkit-1.2-patch.jar:lib/wb-api-2.5.4-nehta-1-SNAPSHOT.jar:lib/wb-bdb-2.5.4-nehta-1-SNAPSHOT.jar:lib/wb-core-2.5.4-nehta-1-SNAPSHOT.jar:lib/wb-foundation-2.5.4-nehta-1-SNAPSHOT.jar:lib/wb-gui-2.5.4-nehta-1-SNAPSHOT.jar:lib/wb-icons-1.0-20100602.054012-1.jar: \
        com.sun.jini.start.ServiceStarter \
            config/start-wb-local.config