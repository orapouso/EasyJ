#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
#! /bin/bash

# Authors:
#      Anahuac de Paula Gil <anahuac@anahuac.biz>
#      Rafael Rodrigues Raposo <orapouso@gmail.com>
#
# Copyright (C) 2011 Anahuac de Paula Gil e Rafael Rodrigues Raposo
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of version 2 of the GNU General Public
# License as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
# USA
#

#Colocar cor nas mensagens de sucesso ou erro
#Fazer verificação de atualização ou criação de banco contando as linhas das tabelas e comparando no final

# Variáveis globais
TOMCAT_FIND="/etc /opt"
drop=0
log=0
verbose=0
dbuser=
dbpass=
TOMCAT_PATH=
dbdump=
LOG_PATH=${symbol_dollar}{log4j.fileappender.path}
APP_PATH=${symbol_dollar}{webapp.path}
DB_NAME=${symbol_dollar}{webapp.db.name}

console() {
	if [ "$2" = "1" ]; then
		echo -e "[\e[1;31m Fail \e[0m] $1"
	elif [ "$2" = "0" ]; then
		echo -e "[\e[1;32m  OK  \e[0m] $1"
	elif [ $verbose -eq 1 ]; then
		echo -e "[ Info ] $1"
	fi
}

usage() {
	echo "Usage: $0 [-h] [-v] [-D] [-L] [-u user] [-p password] [-P TOMCAT_PATH]"
}

show_help() {
	usage
	echo "[-h] Mostra essa ajuda"
	echo "[-v] Verbose"
	echo "[-D] Força DROP do banco de dados"
	echo "[-L] Força exclusão do diretório de log"
	echo "[-u user] Username para conexão com banco de dados"
	echo "[-p password] Senha para conexão com banco de dados"
	echo "[-P tomcat path] Path para a instalação do Tomcat ou path intermediário para ser usado como base da busca: Ex: /opt"
	exit 0
}

get_tomcat_path() {
	console "Caminho do Tomcat não encontrado. Procurando em \e[1;33m$TOMCAT_FIND\e[0m"
	i=0
	for f in `find $TOMCAT_FIND -name catalina.sh -type f -print 2>/dev/null`
	do
		founds[$i]=${symbol_dollar}{symbol_pound}{f/bin\/catalina.sh/}
		let "i += 1"

	done

	TOMCAT_PATH=$founds
	if [ ${symbol_dollar}{#founds[@]} -eq 0 ]; then
		echo ""
		echo -n "Não foi encontrado no seu sistema um caminho válido para o Tomcat. Por favor forneça-o agora ou [a]borte: "
		read TOMCAT_PATH
		echo ""
		if [ "$TOMCAT_PATH" = "a" ]; then
			console "Caminho para o Tomcat não fornecido" 1
			console "Abortando" 1
			exit 1
		fi
	elif [ ${symbol_dollar}{#founds[@]} -gt 1 ]; then
		echo ""
		echo "Foram encontrados os seguintes caminhos para o Tomcat: "
		for (( i = 0 ; i < ${symbol_dollar}{#founds[@]} ; i++ ))
		do
			let "idx = i + 1"
			echo "[$idx] ${symbol_dollar}{founds[$i]}"
		done
		echo -n "Escolha qual instalação do Tomcat deseja usar: "
		read chooseTomcat
		echo ""
		let "chooseTomcat -= 1"
		TOMCAT_PATH=${symbol_dollar}{founds[$chooseTomcat]}
	fi
}

while getopts "hvDLu:p:P:" opt; do
	case $opt in
		h)
			show_help ;;
		v)
			verbose=1 ;;
		D)
			drop=1 ;;
		L)
			log=1 ;;
		u)
			dbuser="$OPTARG" ;;
		p)
			dbpass="$OPTARG" ;;
		P)
			TOMCAT_PATH="$OPTARG" ;;
		\?)
			usage
			exit 1 ;;
	esac
done

if [ "$TOMCAT_PATH" = "" ]; then
	# O caminho do Tomcat não foi fornecido, procurar no filesystem
	get_tomcat_path
	console "Caminho encontrado para o Tomcat: \e[1;31m$TOMCAT_PATH\e[0m"
fi

if [ "$TOMCAT_PATH" != "" -a ! -f $TOMCAT_PATH/bin/catalina.sh ]; then
	# O caminho do Tomcat foi fornecido, mas não é um caminho válido. Usar como base de busca
	TOMCAT_FIND=$TOMCAT_PATH
	get_tomcat_path
	console "Caminho informado para o Tomcat: \e[1;31m$TOMCAT_PATH\e[0m"
else
	console "Caminho informado para o Tomcat: \e[1;31m$TOMCAT_PATH\e[0m"
fi

if [ ! -f $TOMCAT_PATH/bin/catalina.sh ]; then
	console "O caminho fornecido para o Tomcat não parece válido. Por favor verifique." 1
	console "Caminho fornecido: $TOMCAT_PATH" 1
	console "Abortando" 1
	echo ""
	usage
	exit 1
fi

if [ ! -d $TOMCAT_PATH/webapps$APP_PATH ]; then
	create=1
fi

console "Caminho para Tomcat válido" 0

main() {
	#Parando Tomcat
	stop_tomcat
        #Apaga diretório de log
        remove_log_dir
	#Fazendo deploy da aplicação
	undeploy
	#Cria ou atualiza banco de dados
	drop_database
	#Iniciando tomcat
	start_tomcat

	console "DESINSTALAÇÃO CONCLUÍDA COM SUCESSO" 0
}

stop_tomcat() {
	TOM_PID=`get_tomcat_pid`
	if [ "$TOM_PID" != "" ] ; then
		# Parando o Gato Antônio
		console "Parando Tomcat"

		if [ -f /etc/init.d/tomcat ] ; then
			/etc/init.d/tomcat stop > /dev/null
		else
			$TOMCAT_PATH/bin/shutdown.sh > /dev/null
		fi

		sleep 5
	fi

	# Testando se o Gato Anônio está parado
	TOM_PID=`get_tomcat_pid`
	if [ "$TOM_PID" != "" ] ; then
		console "Não foi possível verificar se o Tomcat parou. Caso tenha parado, rode o script novamente, caso contrário, pare-o e depois rode o script" 1
		console "Abortando" 1
		exit 1
	fi

	console "Tomcat parado" 0
}

remove_log_dir() {
	#Criando diretório de log
        if [ $log -eq 1 ] ; then
            if [ -d $LOG_PATH ] ; then
                    console "Removendo diretório de log"
                    rm $LOG_PATH -Rf 2>/dev/null
                    if [ -d $LOG_PATH ] ; then
                        console "Não foi possível remover o diretório de log em $LOG_PATH" 1
                        console "É possível que isso seja devido a falta de permissão" 1
                        console "Execute rm -rf $LOG_PATH com outro usuário" 1
                        console "E depois execute o script uninstall.sh novamente" 1
                        console "Abortando" 1
                        exit 1
                    fi
            fi
            console "Diretório de log removido" 0
        fi
}

undeploy() {
	# Removendo instalação anterior
	if [ -d $TOMCAT_PATH/webapps$APP_PATH ]; then
            console "Removendo instalação anterior"
            rm $TOMCAT_PATH/work/Catalina/localhost$APP_PATH* -Rf 2>/dev/null
            rm $TOMCAT_PATH/conf/Catalina/localhost$APP_PATH* -Rvf 2>/dev/null
            rm $TOMCAT_PATH/webapps$APP_PATH* -Rf 2>/dev/null
            rm bin$APP_PATH -Rf 2>/dev/null

            if [ -d $TOMCAT_PATH/webapps$APP_PATH ] ; then
                    console "Houve erro na ao tentar remover a aplicação. Favor verificar" 1
                    console "Abortando" 1
                    exit 1
            fi
        fi
	console "Aplicação removida" 0
}

drop_database() {
        if [ $drop -eq 1 ] ; then
            console "Removendo banco de dados"
            dump_db

  	`mysql -u$dbuser -p$dbpass $DB_NAME 2>/dev/null <<-MYSQL_INPUT
                DROP SCHEMA IF EXISTS $DB_NAME ;
	MYSQL_INPUT
	`
            console "Banco de dados removido" 0
	fi
}

start_tomcat() {
	# Iniciando o Gato Antônio
	console "Iniciando Tomcat"

	if [ -f /etc/init.d/tomcat ] ; then
		/etc/init.d/tomcat start > /dev/null
	else
		$TOMCAT_PATH/bin/startup.sh > /dev/null
	fi

	# Testando se o Gato Anônio está rodando
	TOM_PID=`get_tomcat_pid`
	if [ "$TOM_PID" = "" ] ; then
		console "Não foi possível verificar se o Tomcat foi reiniciado. Caso tenha iniciado, ignore este aviso, caso contrário inicie-o." 1
		console "Abortando" 1
		exit 1
	fi
	console "Tomcat iniciado" 0
}

#Funções auxiliares
get_tomcat_pid() {
	echo `ps aux | grep catalina | grep -v grep | tr -s ' ' | cut -d" " -f2`
}

dump_db() {
	#Pega credenciais do banco
	get_mysql_connect_info

        if [ ! -d dumps ]; then
                mkdir dumps
        fi

        cd database/dumps
        today=`date +%Y%m%d.%H%M%S`
        dbdump="$DB_NAME_dump_$today.sql"

        console "Criando dump do banco de dados em $dbdump"

        mysqldump -u$dbuser -p$dbpass --add-drop-database --databases $DB_NAME > $dbdump

        if [ "$?" != "0" -o ! -f $dbdump ]; then
                console "Não foi possível criar o dump do banco de dados existente. Favor verificar." 1
                console "Abortando" 1
                exit 1
        fi
        cd ../..

        console "Dump criado" 0
}

get_mysql_connect_info() {
	if [ "$dbuser" = "" ]; then
		echo ""
		#Pega credenciais do banco, username e password
		echo -n "Informe o usuário para conectar no banco de dados: [root] "
		read dbuser
		if [ "$dbuser" = "" ] ; then
			dbuser="root"
		fi
	fi
	if [ "$dbpass" = "" ]; then
		echo -n "Informe a senha para se conectar ao banco de dados: "
		stty -echo
		read dbpass
		stty echo
		echo ""
		echo ""
	fi
}

main $1
