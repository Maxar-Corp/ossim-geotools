dbQuoted=\"$1\"
psql -U postgres -c "drop database $dbQuoted"
psql -U postgres -c "create database $dbQuoted"
psql -U postgres -c "create extension postgis" $1


