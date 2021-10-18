package io.funxion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DataTable
 */
//@WebServlet("/DataTable")
public class DataTable extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SELECT = "select * from ?";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
		Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String value = req.getParameter(parameterName);
            System.out.printf("parameter name is %s, value is %s\n",parameterName,value);            
        }
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.addHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");

		String tableName = request.getParameter("name");
		StringBuilder sb = new StringBuilder();
		String header = """
			<!DOCTYPE html>
			<html>
			<head>
			<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.5.2/css/bootstrap.css">
			<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.11.3/css/dataTables.bootstrap4.min.css">
			<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/buttons/2.0.1/css/buttons.dataTables.min.css">	
			<link rel="stylesheet" type="text/css" href="static/app.css">
			<script type="text/javascript" language="javascript" src="https://code.jquery.com/jquery-3.5.1.js"></script>
			<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/1.11.3/js/jquery.dataTables.js"></script>
			<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/1.11.3/js/dataTables.bootstrap4.min.js"></script>
			<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/buttons/2.0.1/js/dataTables.buttons.min.js"></script>
			<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js" integrity="sha384-Piv4xVNRyMGpqkS2by6br4gNJ7DXjqk09RmUpJ8jgGtD7zP9yug3goQfGII0yAns" crossorigin="anonymous"></script>			
			<script src='https://kit.fontawesome.com/a076d05399.js' crossorigin='anonymous'></script>

			<script type="text/javascript">	
				$(document).ready(function() {
					//$("#datatable").DataTable();
					
					$('#datatable').DataTable({
				        dom: 'Bfrtip',
				        buttons: [
				            {
				                text: 'Add New',
				                action: function ( e, dt, node, config ) {
				                     $('#modal-edit').modal('show');
				                }
				            }
				        ]
				     });
					$("table i").click(function(){
				         $('#modal-edit').modal('show');
				    }); 
					/*
				    $('#datatable').DataTable({
				      processing: true,
				      serverSide: true,
				      ajax: {
				        type: "POST",
				        contentType: "application/json; charset=utf-8",
				        url: "/data",
				        data: function (d) {
				          return JSON.stringify({ parameters: d });
				        }
				      }
				    });
					*/
				} );
			</script>			
			<script type="text/javascript" language="javascript" src="static/app.js"></script>
			</head>
			<body style="margin:20px">	
		""";
		sb.append(header);
		List<String> dataTypeColumns = Arrays.asList("column_name","data_type");
		String query = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = '" + tableName
				+ "';";
		List<ColumnInfo> columns = getTableMetaData(tableName,query,sb,dataTypeColumns);
		renderForm(sb, columns);
		sb.append("<hr/>");
		renderTable(tableName,"SELECT * FROM "+tableName,sb,columns);
		
		sb.append(""" 
			</body>
			</html>
		""");
		response.getWriter().append(sb.toString());
	}

	private List<ColumnInfo> getTableMetaData(String tableName, String query, StringBuilder sb, List<String> columnLis) {
		
		// Using try-with-resources for auto closing connection, pstmt, and rs.
		List<ColumnInfo> columnList = new ArrayList<>();
		try (Connection connection = PGDataSource.getConnection();
				PreparedStatement pstmt = connection.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery();) {
			while (rs.next()) {
				ColumnInfo info = new ColumnInfo();
				info.column_name = rs.getString("column_name");
				info.data_type = rs.getString("data_type");
				columnList.add(info);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return columnList;
	}

	private void renderForm(StringBuilder sb, List<ColumnInfo> columns) {
		/*
		 * <form>
  <div class="form-group">
    <label for="formGroupExampleInput">Example label</label>
    <input type="text" class="form-control" id="formGroupExampleInput" placeholder="Example input">
  </div>
  <div class="form-group">
    <label for="formGroupExampleInput2">Another label</label>
    <input type="text" class="form-control" id="formGroupExampleInput2" placeholder="Another input">
  </div>
</form>
		 */
		String popup = """
				<div id="modal-edit" class="modal" tabindex="-1" role="dialog">
				      <div class="modal-dialog" role="document">
				         <div class="modal-content">
				            <div class="modal-header">
				              <h5 class="modal-title">Edit Record</h5>
				              <button type="button" class="close" data-dismiss="modal" aria-label="Close">
				                <span aria-hidden="true">&times;</span>
				              </button>
				            </div>
				            <div class="modal-body">
							<form>
				               __FORM_CONTENT__
							</form>
				            </div>
				            <div class="modal-footer">
				              <button type="submit" class="btn btn-primary">Save</button>
				              <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
				            </div>
				         </div>
				      </div>				      
				   </div>
				""";
		//sb.append("<form>");
		StringBuilder sbForm = new StringBuilder();
		for (ColumnInfo column : columns) {
			sbForm.append("<div class=\"form-group\">");
			sbForm.append("<label for=\""+column.column_name+"\">"+column.column_name+"</label>");
			switch(column.data_type) {
			case "text":
			case "integer":
			case "character":
				sbForm.append("<input type=\"text\" class=\"form-control\" id=\""+column.column_name+"\">");				
			}
			sbForm.append("</div>");
		}
		String popupString = popup.replaceFirst("__FORM_CONTENT__", sbForm.toString());
		sb.append(popupString);
	}
	private void renderTable(String tableName, String query, StringBuilder sb, List<ColumnInfo> columns) {
		sb.append("\r\n").append("<table id=\"datatable\" class=\"table table-striped table-bordered\">").append("<thead class=\"thead-light\">").append("<tr>");
		for (ColumnInfo column : columns) {
			sb.append("<th>").append(column.column_name).append("</th>");
		}
		sb.append("<th>").append("Actions").append("</th>");
		sb.append("</tr></thead>").append("<tbody>");
		
		// Using try-with-resources for auto closing connection, pstmt, and rs.
		
		try (Connection connection = PGDataSource.getConnection();
				PreparedStatement pstmt = connection.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery();) {
			while (rs.next()) {
				sb.append("<tr>");
				for (ColumnInfo column : columns) {
					sb.append("<td>").append(rs.getString(column.column_name)).append("</td>");
				}
				sb.append("<td>").append("<i class='fas fa-edit'/><i class='fas fa-trash-alt' style='padding-left:20px;'/>").append("</td>");
				sb.append("</tr>");				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		sb.append("</tbody>").append("</table>");
	}

}
