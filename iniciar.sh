#!/bin/sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
if [ ! -f config/database.properties ]; then
    (umask 077; cp config/database.properties.example config/database.properties)
    echo 'Completa tu clave MySQL en config/database.properties y vuelve a ejecutar.'
    exit 1
fi
if [ -f Soto_Nunez_Yaxon_PC2.jar ]; then
    exec java -jar Soto_Nunez_Yaxon_PC2.jar
fi
exec java -jar target/Soto_Nunez_Yaxon_PC2.jar
