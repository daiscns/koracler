import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Statement
import java.util.LinkedHashMap

import org.yaml.snakeyaml.Yaml;

val Lines_conf = """# configuration file
  #   [...]: default value
  #   'y': yes or on(switched), 'n': no or off

'Class_forName': 'org.sqlite.JDBC'
  # for sqlite: 'org.sqlite.JDBC'
  # for oracle: 'oracle.jdbc.driver.OracleDriver'
'uri_DriverManager': 'jdbc:sqlite:test.db'
  # for sqlite: 'jdbc:sqlite:test.db'
  # for oracle:
  #   'jdbc:oracle:thin:@111.222.333.444:1521:sid'
  #   'u/p@ip/sid'
  #     to see 'sid': SQL> select sys_context('USERENV','INSTANCE_NAME') from dual;
'dba_user': 'ora'
'dba_pw': 'cle'
'sql':
  - select count(*) from muser
  - |
    select * from muser
      where name!='yy'
  # do NOT put ';' semi-column as the end of sql: oracle can NOT read it...

'query_timeout': 5
'safety_counts': 100000
'safety_seconds': 6
'file_name_out': 'out.txt'

# vvvvvv not implemented yet below vvvvvv
"""

fun mk_params(): MutableMap<*, *> {
	val flnm_conf = "koracler_conf.yaml"
	val fl_conf = File(flnm_conf)
	if (! fl_conf.exists() ){
		fl_conf.writeText(Lines_conf)
		println("[info] made config file: ${fl_conf}")
		println("         please edit this file to use this app")
		kotlin.system.exitProcess(0)
	}
	try{
		val text_whole = fl_conf.readText()
		val yaml = Yaml()
		val list: MutableMap<*, *> = yaml.load( FileInputStream( File(flnm_conf) ) ) as LinkedHashMap<*, *>
		return list
/*
{exec_sqlplus=sqlplus -s, uri_oracle=u/p@ip/s, sql=[select count(*) from test
  where xx='yy';
, select * from test
  where xx='yy';
], safety_counts=100000, safety_seconds=2, file_name_out=out.txt}
*/
	} catch (e: Exception) {
		println(e)
		throw e
	}
}
fun do_dba() {
	val params = mk_params()
//		println( params.size )		println( params.keys )		println( params.get("sql"))
	val sqls: List<String> = params.get("sql") as List<String>
//		println( sqls[1] )		println( params.get("Class_forName") )
	val class_forName = params.get("Class_forName") as String
	val query_timeout = (params.get("query_timeout") as Int?)?: 30
	val safety_counts = (params.get("safety_counts") as Int?)?: 100000
	val safety_seconds = (params.get("safety_seconds") as Int?)?: 20
	val file_name_out 		= params.get("file_name_out") as String
	val uri_DriverManager 	= params.get("uri_DriverManager") as String
	val dba_user 				= params.get("dba_user") as String
	val dba_pw 					= params.get("dba_pw") as String
	var flg_overtime			= false;

	Class.forName(class_forName)
	try {
		val conn = if(class_forName=="org.sqlite.JDBC"
							) DriverManager.getConnection(uri_DriverManager)
						else DriverManager.getConnection(uri_DriverManager, dba_user, dba_pw )
		val sttm = conn.createStatement()
		for( sql in sqls ){
			sttm.setQueryTimeout(query_timeout)
			var rs: ResultSet = sttm.executeQuery(sql)
			var rsmd: ResultSetMetaData = rs.getMetaData()
			var cnt: Int = 0
			val msec_started = (System.currentTimeMillis() as Long)?: 0L
			while (rs.next()) {
				for (i in IntRange(1, rsmd.getColumnCount())) {
					print(rs.getString(i))
					print( if(i < rsmd.getColumnCount()) "," else "")
				}
				print(System.getProperty("line.separator"))
				cnt++
				if(safety_counts<cnt) break;
				println("  safety_sec: ${safety_seconds}, during_msec: ${System.currentTimeMillis() - msec_started}")
				if(safety_seconds*1000L < System.currentTimeMillis() - msec_started){
					flg_overtime = true
					break
				}
			}
			println(" rs size: ${cnt}")
			if(safety_counts<cnt){
				println("[warn] query records has been over the limit: ${cnt}")
				break;
			}
			if(flg_overtime){
				println("[warn] query time has been over the limit: ${safety_seconds} seconds")
				break;
			}
		}
		sttm.close()
		conn.close()
	} catch (e: Exception) {
		println(e)
	}
}
fun main(args: Array<String>) {
	do_dba()
}
