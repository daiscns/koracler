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

'Class_forName': 'oracle.jdbc.driver.OracleDriver'
  # for oracle: 'oracle.jdbc.driver.OracleDriver'
  # for sqlite: 'org.sqlite.JDBC'
'uri_DriverManager': 'jdbc:sqlite:test.db'
  # for oracle:
    # 'jdbc:oracle:thin:@111.222.333.444:1521:'
    # 'u/p@ip/s'
  # for sqlite: 'jdbc:sqlite:test.db'
'sql':
  - select count(*) from muser;
  - |
    select * from muser
      where name!='yy';
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
	Class.forName( params.get("Class_forName") as String )
	try {
		val conn = DriverManager.getConnection( params.get("uri_DriverManager") as String )
		val sttm = conn.createStatement()
		for( sql in sqls ){
			sttm.setQueryTimeout(30)
			var rs: ResultSet = sttm.executeQuery(sql)
			var rsmd: ResultSetMetaData = rs.getMetaData()
			var cnt: Int = 0
			while (rs.next()) {
				for (i in IntRange(1, rsmd.getColumnCount())) {
					print(rs.getString(i))
					print( if(i < rsmd.getColumnCount()) "," else "")
				}
				print(System.getProperty("line.separator"))
				cnt++
			}
			
			println(" rs size: ${cnt}")
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
