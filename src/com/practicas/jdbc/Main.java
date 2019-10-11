package com.practicas.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;
import java.math.BigDecimal;
import java.io.Console;

public class Main {

	public static void main(String[] args) {
		String cadConexion = "jdbc:mysql://localhost:3306/";
		String bd = "Empresa";
		String usuario = "root";
		
		// Password
		// Console console = System.console(); // No funciona en Eclipse
        // String pass = new String(console.readPassword("Password: ")); // No funciona en Eclipse
		String pass = ""; // Poner el password aquí
		
		// Campos de la tabla Proveedores
		String[] proveedoresFields = {"idProveedor","nif","nombre","direccion"};
		int[] proveedoresTypes = {0,1,1,1}; // 0-Key(int), 1-String
		// Campos de la tabla Productos
		String[] productosFields = {"idProducto","codigo","nombre","precioUnitario","idProveedor"};
		int[] productosTypes = {0,1,1,2,3}; // 0-Key(int), 1-String, 2-double, 3-int
		// Nota: se podria usar getMetaData para leer directamente los nombres y tipos de datos de los campos,
		//       pero para esta primera práctica de JDBC creo que ya está bien así

		try {
			Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
			Connection con = DriverManager.getConnection(cadConexion + bd + "?useSSL=false", usuario, pass);
			Scanner entrada = new Scanner(System.in);
			int res;
			
			do {
				// Menú Principal
				System.out.println("MENÚ PRINCIPAL - Escoja opción:");
				System.out.println("1.- Proveedores");
				System.out.println("2.- Productos");
				System.out.println("0.- Salir");
				res = Integer.parseInt(entrada.nextLine());

				switch (res) {
				case 1:
					// CRUD Proveedores
					doCRUD("Proveedores", proveedoresFields, proveedoresTypes, entrada, con);
					break;
				case 2:
					// CRUD Productos
					doCRUD("Productos", productosFields, productosTypes, entrada, con);
					break;
				}
			} while (res != 0);
			con.close();
			entrada.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private static void doCRUD(String table, String[] tableF, int[] tableT, Scanner in, Connection co) {
		Statement stmt;
		PreparedStatement pstmt;
		ResultSet rs;
		int res;
		String outStr;
		int id;
		String fStr, vStr;
		String inStr;
		int inInt;
		BigDecimal inDec;
		
		// Menú CRUD
		try {
			do {
				System.out.println("\n" + table.toUpperCase() + " - Escoja opción:");
				System.out.println("1.- Ver registros");
				System.out.println("2.- Añadir registro");
				System.out.println("3.- Modificar registro");
				System.out.println("4.- Eliminar registro");
				System.out.println("0.- Salir");
				res = Integer.parseInt(in.nextLine());
			
				switch (res) {
				case 1:
					// ******* VER REGISTROS *******
					// Mostrar nombres de los campos
					outStr = "";
					for(int i=0; i<tableF.length; i++) {
						outStr = outStr.concat(tableF[i] + "\t");
					}
					System.out.println(outStr);
					// Mostrar registros
					stmt = co.createStatement();
					rs = stmt.executeQuery("SELECT * FROM " + table);
					while (rs.next())
						mostrarRegistro (rs, tableF, tableT);
					break;
				case 2:
					// ******* AÑADIR REGISTRO *******
					// Nombres de los campos
					fStr = "";
					vStr = "";
					for(int i=1; i<tableF.length; i++) {
						fStr = fStr.concat(tableF[i] + ",");
						vStr = vStr.concat("?,");
					}
					if(fStr.length() > 0) {
						fStr = fStr.substring(0,fStr.length()-1);
						vStr = vStr.substring(0,vStr.length()-1);
						pstmt = co.prepareStatement("INSERT INTO " + table + " (" + fStr + ") VALUES (" + vStr + ")");
						// Valores de los campos
						for(int i=1; i<tableF.length; i++) {
							// Entrar valor
							System.out.println("Introduzca " + tableF[i].toUpperCase() + ": ");
							switch (tableT[i]) {
							// 1-String, 2-BigDecimal, 3-int
							case 1:
								inStr = in.nextLine();
								pstmt.setString(i, inStr);
								break;
							case 2:
								inDec = new BigDecimal(in.nextLine());
								pstmt.setBigDecimal(i, inDec);
								break;
							case 3:
								inInt = Integer.parseInt(in.nextLine());
								pstmt.setInt(i, inInt);
								break;
							}
						}
						pstmt.execute();
						System.out.println(table.toUpperCase() + "- Registro insertado.");
					}
					break;
				case 3:
					// ******* MODIFICAR REGISTRO *******
					// Seleccionar el registro a modificar
					System.out.println("Introduzca el id del registro que quiere modificar: ");
					id = Integer.parseInt(in.nextLine());
					// MOSTRAR REGISTRO A MODIFICAR
					pstmt = co.prepareStatement("SELECT * FROM " + table + " WHERE " + tableF[0] + "=?");
					pstmt.setInt(1, id);
					rs = pstmt.executeQuery();
					if(!rs.next()) {
						System.out.println("ERROR: id " + id + " no existe.");
						break;
					}
					// Mostrar nombres de los campos
					outStr = "";
					for(int i=0; i<tableF.length; i++) {
						outStr = outStr.concat(tableF[i] + "\t");
					}
					System.out.println(outStr);
					// Mostrar valores registro
					mostrarRegistro (rs, tableF, tableT);
					// ENTRAR NUEVOS VALORES
					System.out.println("Introduzca los nuevos valores");
					// Nombres de los campos
					fStr = "";
					for(int i=1; i<tableF.length; i++)
						fStr = fStr.concat(tableF[i] + "=?,");
					if(fStr.length() > 0) {
						fStr = fStr.substring(0,fStr.length()-1);
						pstmt = co.prepareStatement("UPDATE " + table + " SET " + fStr + " WHERE " + tableF[0] + "=?");
						// Valores de los campos
						for(int i=1; i<tableF.length; i++) {
							// Entrar valor
							System.out.println("Introduzca " + tableF[i].toUpperCase() + ": ");
							switch (tableT[i]) {
							// 1-String, 2-BigDecimal, 3-int
							case 1:
								inStr = in.nextLine();
								pstmt.setString(i, inStr);
								break;
							case 2:
								inDec = new BigDecimal(in.nextLine());
								pstmt.setBigDecimal(i, inDec);
								break;
							case 3:
								inInt = Integer.parseInt(in.nextLine());
								pstmt.setInt(i, inInt);
								break;
							}
						}
						pstmt.setInt(tableF.length, id);
						pstmt.execute();
						System.out.println(table.toUpperCase() + "- Registro modificado.");
					}
					break;
				case 4:
					// ******* ELIMINAR REGISTRO *******
					// Seleccionar el registro a eliminar
					System.out.println("Introduzca el id del registro que quiere eliminar: ");
					id = Integer.parseInt(in.nextLine());
					// MOSTRAR REGISTRO A ELIMINAR
					pstmt = co.prepareStatement("SELECT * FROM " + table + " WHERE " + tableF[0] + "=?");
					pstmt.setInt(1, id);
					rs = pstmt.executeQuery();
					if(!rs.next()) {
						System.out.println("ERROR: id " + id + " no existe.");
						break;
					}
					// Mostrar nombres de los campos
					outStr = "";
					for(int i=0; i<tableF.length; i++) {
						outStr = outStr.concat(tableF[i] + "\t");
					}
					System.out.println(outStr);
					// Mostrar valores registro
					mostrarRegistro (rs, tableF, tableT);
					// CORFIRMAR ELIMINAR REGISTRO
					System.out.println("¿Realmente quiere eliminar este registro? (SI/NO): ");
					inStr = in.nextLine();
					if(inStr.equalsIgnoreCase("SI")) {
						pstmt = co.prepareStatement("DELETE FROM " + table + " WHERE " + tableF[0] + "=?");
						pstmt.setInt(1, id);
						pstmt.execute();
						System.out.println(table.toUpperCase() + " - Registro eliminado");
					}
					break;
				}
			
			} while (res != 0);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private static void mostrarRegistro (ResultSet r, String[] tF, int[] tT) {
		// Mostrar Datos de un Registro
		String str = "";
		
		try {
			for(int i=0; i<tT.length; i++) {
				switch (tT[i]) {
				// 0-Key(int), 1-String, 2-BigDecimal, 3-int
				case 0: 
					str = str.concat(r.getInt(tF[i]) + "\t");
					break;
				case 1:
					str = str.concat(r.getString(tF[i]) + "\t");
					break;
				case 2: 
					str = str.concat(r.getBigDecimal(tF[i]) + "\t");
					break;
				case 3: 
					str = str.concat(r.getInt(tF[i]) + "\t");
					break;
				}
			}
			System.out.println(str);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

}

