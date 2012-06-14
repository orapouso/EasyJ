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
globalPatchChoice=0
patchApplied=0
create=0
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
	echo "Usage: $0 [-h] [-v] [-c] [-u user] [-p password] [-P TOMCAT_PATH]"
}

show_help() {
	usage
	echo "[-h] Mostra essa ajuda"
	echo "[-v] Verbose"
	echo "[-c] Força criação mesmo já existindo versão anterior"
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
		founds[$i]=${symbol_dollar}{f/bin\/catalina.sh/}
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

while getopts "hcvu:p:P:" opt; do
	case $opt in
		h)
			show_help ;;
		v)
			verbose=1 ;;
		c)
			create=1 ;;
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

if [ ! -d $TOMCAT_PATH/webapps/$APP_PATH ]; then
	create=1
fi

console "Caminho para Tomcat válido" 0

main() {

	#Verificando se existe o script de inicialização do Tomcat. Se não existe, copia
	check_tomcat_script
	#Parando Tomcat
	stop_tomcat
	#Criando diretório de log se necessário
	create_log_dir
	#Explodindo arquivo war para futuro deploy
	explode_war_file
	#Copiando configurações locais de instalações anteriores
	copy_local_config
	#Cria ou atualiza banco de dados
	install_database
	#Fazendo deploy da aplicação
	deploy
	#Iniciando tomcat
	start_tomcat

	console "INSTALAÇÃO CONCLUÍDA COM SUCESSO" 0
}

check_tomcat_script() {
	if [ ! -f /etc/init.d/tomcat ] ; then
		console "Instalando script de inicialização do Tomcat"
		#Copiando script de inicialização para o init.d
		chmod 755 scripts/tomcat

		sed -i "s,PATH_TO_INSTALLED_TOMCAT,$TOMCAT_PATH," scripts/tomcat
		
		cp scripts/tomcat /etc/init.d/ -Rf

		if [ ! -f /etc/init.d/tomcat ]; then
			console "Não foi possível instalar o script de inicialização do Tomcat" 1
			console "Continuando, mas Tomcat não será inicializado no startup da máquina" 1
		else
			#Configurando o script para rodar no startup da máquina
			init=0
			if [ "`which chkconfig`" != "" ]; then
				chkconfig tomcat defaults > /dev/null 2>&1
				init=$?
			elif [ "`which update-rc.d`" != "" ]; then
				update-rc.d tomcat defaults > /dev/null 2>&1
				init=$?
			else
				init=1
			fi

			if [ "$init" != "0" ]; then
				console "Não foi possível colocar o script de inicialização do Tomcat no startup da máquina" 1
				console "Continuando, mas Tomcat não será inicializado no startup da máquina" 1
			else
				console "Script de inicialização instalado" 0
			fi
		fi

	fi
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

create_log_dir() {
	#Criando diretório de log
	if [ -d $LOG_PATH -a "$create" != "1" ] ; then
		console "Diretório de log já existe" 0
	else
		console "Criando diretório de log"
		if [ "$create" = "1" ] ; then
			rm $LOG_PATH -Rf > /dev/null
		fi
		mkdir $LOG_PATH

		#Testando criação do diretório
		if [ $? -ne 0 ] ; then
			console "Não foi possível criar o diretório de log em $LOG_PATH" 1
			console "É possível que isso seja devido a falta de permissão" 1
			console "Execute mkdir $LOG_PATH com outro usuário" 1
			console "E depois execute o script install.sh novamente" 1
			console "Abortando" 1
			exit 1
		fi
		console "Diretorio de log criado" 0
	fi
}

explode_war_file() {
	# Arquivo war existe?
	if [ -f bin/$APP_PATH.war ] ; then
		if [ ! -d bin/$APP_PATH ] ; then
			console "Explodindo $APP_PATH.war"
			cd bin
			mkdir $APP_PATH
			cd $APP_PATH
			cp ../$APP_PATH.war .
			unzip -q $APP_PATH.war
                        rm $APP_PATH.war
			cd ../..
			console "Aquivo $APP_PATH.war explodido" 0
		fi
	else
		console "O arquivo bin/$APP_PATH.war não foi encontrado." 1
		console "Abortando" 1
		exit 1
	fi
}

copy_local_config() {
        cd bin
	if [ -f $TOMCAT_PATH/webapps/$APP_PATH/config/users.properties ] ; then
		# Copiando configuração de usuários
		cp $TOMCAT_PATH/webapps/$APP_PATH/config/users.properties $APP_PATH/config -Rf
		# testando se a copia foi bem feita

		if [ $? -ne 0 ] ; then
			console "Houve algum erro na copia do arquivo snapshot.json. Favor verificar" 1
			console "Abortando" 1
			exit 1
		fi
        fi

	if [ -f $TOMCAT_PATH/webapps/$APP_PATH/WEB-INF/classes/log4j.xml ] ; then
		# Copiando configuração do log4j
		cp $TOMCAT_PATH/webapps/$APP_PATH/WEB-INF/classes/log4j.xml $APP_PATH/WEB-INF/classes -Rf
		# testando se a copia foi bem feita

		if [ $? -ne 0 ] ; then
			console "Houve algum erro na copia do arquivo snapshot.json. Favor verificar" 1
			console "Abortando" 1
			exit 1
		fi
        fi
        cd ..
}

install_database() {
	cd database
	#Verificando se existe app ja instalado
	#Se não existe versão anterior cria novo banco de dados
	if [ $create -eq 1 ] ; then
		create_db
	else
	#Se existe faz um upgrade de versão
		upgrade_db
	fi
	cd ..
}

deploy() {
	# Copiando o app novo
	console "Instalando nova versão"
	cp bin/$APP_PATH $TOMCAT_PATH/webapps -Rf
	if [ ! -d $TOMCAT_PATH/webapps/$APP_PATH ]; then
		console "Houve erro na instalação da aplicação. Favor verificar" 1
		console "Abortando" 1
	fi
	console "Aplicação instalada" 0
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
		console "Não foi possível verificar se o Tomcat foi iniciado. Caso tenha iniciado, ignore este aviso, caso contrário inicie-o." 1
		console "Abortando" 1
		exit 1
	fi
	console "Tomcat iniciado" 0
}

#Funções auxiliares
get_tomcat_pid() {
	echo `ps aux | grep catalina | grep -v grep | tr -s ' ' | cut -d" " -f2`
}

create_db() {
	
	dump_db

	console "Criando banco de dados"
	#Aplica o script de criação do banco
	mysql -u$dbuser -p$dbpass < "create_$DB_NAME.sql" 2>/dev/null

	if [ "$?" != "0" ] ; then
		console "Houve algum erro ao tentar criar o banco de dados para o $DB_NAME. Verificar usuário e senha." 1
		return_dump
		start_tomcat
		console "Abortando" 1
		exit 1
	fi

	console "Criação do banco de dados feita com sucesso" 0
}

upgrade_db() {
	#Se já existe uma versão, executa scripts de patch
	check_apply_patch	

	# Movendo o app original para /tmp
	console "Removendo instalação anterior"
	rm $TOMCAT_PATH/work/Catalina/localhost/$APP_PATH* -Rf
	cp $TOMCAT_PATH/webapps/$APP_PATH* /tmp -Rf
	rm $TOMCAT_PATH/webapps/$APP_PATH* -Rf

	# Testando a "movida"
	if [ -d $TOMCAT_PATH/webapps/$APP_PATH ]; then
		console "Houve algum erro ao mover o diretório original do $APP_PATH. Favor verifique" 1
		console "Abortando" 1
		exit 1
	fi

	console "Instalação anterior removida" 0
}

check_apply_patch(){
	version=`cat $TOMCAT_PATH/webapps/$APP_PATH/version`
	version=( ${symbol_dollar}{version//./ } )
	for p in $(ls -1 patch*.sql 2>/dev/null | sort)
	do
		if [ $patchApplied -eq 1 ] ; then
			apply_patch $p
		else
			filename="${symbol_dollar}{p%.*}"
			filename="${symbol_dollar}{filename:9}"
			v=( ${symbol_dollar}{filename//./ } )
			if [ ${symbol_dollar}{v[0]} -gt ${symbol_dollar}{version[0]} ] ; then
				apply_patch $p
			elif [ ${symbol_dollar}{v[0]} -eq ${symbol_dollar}{version[0]} ] ; then
				if [ ${symbol_dollar}{v[1]} -gt ${symbol_dollar}{version[1]} ] ; then
					apply_patch $p
				elif [ ${symbol_dollar}{v[1]} -eq ${symbol_dollar}{version[1]} ] ; then
					if [ ${symbol_dollar}{v[2]} -gt ${symbol_dollar}{version[2]} ] ; then
						apply_patch $p
					fi
				fi
			fi
		fi
	done
}

apply_patch() {
	patch=$1
	echo ""
	echo -n "Encontrado arquivo de patch $patch"
	if [ $globalPatchChoice -eq 0 ] ; then
		echo " deseja aplicá-lo?"
		echo -n "[s]im, [n]ão, sim para [t]odos: "
		read choice

		apply=0
		case "$choice" in
			"s") apply=1 ;;
			"t") apply=1; globalPatchChoice=1 ;;
		esac		
	else
		echo ""
		apply=1
	fi

	if [ $apply -eq 1 ] ; then
		#Se for aplicar o patch mas for o primeiro, criar dump do banco
		if [ $patchApplied -ne 1 ] ; then
			dump_db
		fi

		console "Aplicando patch $patch"
		#Aplica o script de criação do banco
		mysql -u$dbuser -p$dbpass < $patch 2>/dev/null
	
		if [ "$?" != "0" ] ; then
			console "Houve algum erro ao tentar aplicar o patch no banco de dados. Verificar usuário e senha ou algum erro no patch." 1
			return_dump
			start_tomcat
			console "Abortando" 1
			exit 1
		fi

		patchApplied=1

		console "Patch aplicado com sucesso" 0
	fi
}

dump_db() {
	#Pega credenciais do banco
	get_mysql_connect_info

	count=`get_monitorable_count`
	if [ $count -ge 2 ]; then
		if [ ! -d dumps ]; then
			mkdir dumps
		fi

		cd dumps
		today=`date +%Y%m%d.%H%M%S`
		dbdump="$DB_NAME_dump_$today.sql"

                console "Criando dump do banco de dados em $dbdump"

		mysqldump -u$dbuser -p$dbpass --add-drop-database --databases $DB_NAME > $dbdump

		if [ "$?" != "0" -o ! -f $dbdump ]; then
			console "Não foi possível criar o dump do banco de dados existente. Favor verificar." 1
			console "Abortando" 1
			exit 1
		fi
		cd ..

		console "Dump criado" 0
	fi
}

return_dump() {
	if [ "$dbdump" != "" ]; then
		console "Retornando banco ao estado do dump"

		#Aplica o script de criação do banco
		cd dumps
		mysql -u$dbuser -p$dbpass < $dbdump 2>/dev/null

		if [ "$?" != "0" ] ; then
			console "Houve algum erro ao tentar retornar o banco para o estado do dump. Favor verificar." 1
		fi

		cd ..
		console "Dump retornado com sucesso" 0
	fi
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
