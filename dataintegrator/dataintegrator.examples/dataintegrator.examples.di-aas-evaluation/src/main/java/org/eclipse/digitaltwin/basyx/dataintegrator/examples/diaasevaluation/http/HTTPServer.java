package org.eclipse.digitaltwin.basyx.dataintegrator.examples.diaasevaluation.http;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.apache.http.HttpStatus;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

public class HTTPServer {
	
	private int port;
	private MockServerClient mockServer;
	
	public HTTPServer(int port) {
		this.port = port;
	}
	
	public void start() {
		mockServer = ClientAndServer.startClientAndServer(port);

		createExpectationForGetRequest();
	}
	
	public void stop() {
		mockServer.stop();
	}
	
	@SuppressWarnings("resource")
	private void createExpectationForGetRequest() {
		new MockServerClient("localhost", port)
				.when(request().withMethod("GET").withPath("/sample/data").withHeader("Content-Type", "application/json"))
				.respond(response().withStatusCode(HttpStatus.SC_OK).withBody("{\r\n"
						+ "    \"request_fields\": {\r\n"
						+ "        \"name\": \"Orb Intelligence\",\r\n"
						+ "        \"orb_num\": 23248971\r\n"
						+ "    },\r\n"
						+ "    \"results_count\": 1,\r\n"
						+ "    \"results\": [\r\n"
						+ "        {\r\n"
						+ "            \"result_number\": 1,\r\n"
						+ "            \"orb_num\": 64819790,\r\n"
						+ "            \"name\": \"ORB INTELLIGENCE, INC\",\r\n"
						+ "            \"entity_type\": \"company\",\r\n"
						+ "            \"company_status\": \"active\"\r\n"
						+ "        }\r\n"
						+ "    ],\r\n"
						+ "    \"address\": {\r\n"
						+ "        \"address0\":\"P.O. Box 194, 1317 Sit Av.\",\r\n"
						+ "        \"address1\":\"Ap #404-3682 Penatibus Road\",\r\n"
						+ "        \"address2\":\"3206 Vulputate, Rd.\",\r\n"
						+ "        \"address3\":\"Ap #600-2450 Ac Ave\",\r\n"
						+ "        \"address4\":\"Ap #428-7715 Luctus Av.\",\r\n"
						+ "        \"address5\":\"962-3675 Risus. Avenue\",\r\n"
						+ "        \"address6\":\"Ap #870-3719 Nunc St.\",\r\n"
						+ "        \"address7\":\"Ap #114-3980 Dis Avenue\",\r\n"
						+ "        \"address8\":\"Ap #605-1911 Lacinia. Rd.\",\r\n"
						+ "        \"address9\":\"Ap #748-1126 Urna. Street\",\r\n"
						+ "        \"address10\":\"Ap #915-2251 Suspendisse Street\",\r\n"
						+ "        \"address11\":\"6715 Ac Road\",\r\n"
						+ "        \"address12\":\"Ap #934-5828 Laoreet Road\",\r\n"
						+ "        \"address13\":\"Ap #283-1658 Eget Rd.\",\r\n"
						+ "        \"address14\":\"Ap #220-4450 Natoque Road\",\r\n"
						+ "        \"address15\":\"Ap #470-7423 Non, Street\",\r\n"
						+ "        \"address16\":\"3527 Dui, Ave\",\r\n"
						+ "        \"address17\":\"P.O. Box 357, 9695 Luctus Street\",\r\n"
						+ "        \"address18\":\"648-5138 Nec Rd.\",\r\n"
						+ "        \"address19\":\"865-6166 Nullam Rd.\",\r\n"
						+ "        \"address20\":\"Ap #846-994 Donec Road\",\r\n"
						+ "        \"address21\":\"8474 Netus St.\",\r\n"
						+ "        \"address22\":\"Ap #133-7502 Vivamus Av.\",\r\n"
						+ "        \"address23\":\"3461 Velit Av.\",\r\n"
						+ "        \"address24\":\"Ap #269-8162 Laoreet St.\",\r\n"
						+ "        \"address25\":\"402-2133 Metus Av.\",\r\n"
						+ "        \"address26\":\"Ap #683-7679 Ipsum. Road\",\r\n"
						+ "        \"address27\":\"Ap #771-7286 Quisque Street\",\r\n"
						+ "        \"address28\":\"Ap #999-8490 Ac Rd.\",\r\n"
						+ "        \"address29\":\"Ap #349-9735 Lorem Rd.\",\r\n"
						+ "        \"address30\":\"Ap #774-5758 Dolor Ave\",\r\n"
						+ "        \"address31\":\"Ap #239-742 Quisque Street\",\r\n"
						+ "        \"address32\":\"Ap #296-8371 Elit St.\",\r\n"
						+ "        \"address33\":\"P.O. Box 646, 6269 Nulla Ave\",\r\n"
						+ "        \"address34\":\"663-758 Consectetuer St.\",\r\n"
						+ "        \"address35\":\"Ap #795-1123 Cursus Avenue\",\r\n"
						+ "        \"address36\":\"P.O. Box 831, 4536 In St.\",\r\n"
						+ "        \"address37\":\"Ap #809-5894 Id, Ave\",\r\n"
						+ "        \"address38\":\"4391 Dictum Av.\",\r\n"
						+ "        \"address39\":\"968-7224 Rhoncus St.\",\r\n"
						+ "        \"address40\":\"P.O. Box 754, 5124 Ac Street\",\r\n"
						+ "        \"address41\":\"551-2032 Malesuada Avenue\",\r\n"
						+ "        \"address42\":\"3543 Donec Ave\",\r\n"
						+ "        \"address43\":\"439-7002 Sed Ave\",\r\n"
						+ "        \"address44\":\"288-6922 Dapibus Av.\",\r\n"
						+ "        \"address45\":\"P.O. Box 950, 5106 Aliquam Ave\",\r\n"
						+ "        \"address46\":\"Ap #503-1879 Faucibus Rd.\",\r\n"
						+ "        \"address47\":\"P.O. Box 838, 1908 Fusce St.\",\r\n"
						+ "        \"address48\":\"P.O. Box 951, 1992 Vel St.\",\r\n"
						+ "        \"address49\":\"997-2492 Elit, Street\",\r\n"
						+ "        \"address50\":\"Ap #328-8797 Eu Rd.\",\r\n"
						+ "        \"address51\":\"577-2766 Nullam Road\",\r\n"
						+ "        \"address52\":\"Ap #608-675 Luctus. Rd.\",\r\n"
						+ "        \"address53\":\"7519 Congue, Road\",\r\n"
						+ "        \"address54\":\"Ap #755-1890 Dictum Road\",\r\n"
						+ "        \"address55\":\"862-6425 Nulla Road\",\r\n"
						+ "        \"address56\":\"602-5087 Dictum. Ave\",\r\n"
						+ "        \"address57\":\"176-7988 Vitae Av.\",\r\n"
						+ "        \"address58\":\"Ap #777-8255 Consequat Rd.\",\r\n"
						+ "        \"address59\":\"472-9758 Pharetra. St.\",\r\n"
						+ "        \"address60\":\"Ap #241-9327 Erat, Rd.\",\r\n"
						+ "        \"address61\":\"468-7906 Donec St.\",\r\n"
						+ "        \"address62\":\"Ap #734-540 Libero St.\",\r\n"
						+ "        \"address63\":\"P.O. Box 451, 491 Egestas Rd.\",\r\n"
						+ "        \"address64\":\"6114 Facilisis, Ave\",\r\n"
						+ "        \"address65\":\"9068 In St.\",\r\n"
						+ "        \"address66\":\"Ap #655-9207 Et, Av.\",\r\n"
						+ "        \"address67\":\"Ap #877-2606 Dictum St.\",\r\n"
						+ "        \"address68\":\"389-6102 Feugiat Av.\",\r\n"
						+ "        \"address69\":\"P.O. Box 748, 8200 Nulla Street\",\r\n"
						+ "        \"address70\":\"Ap #919-4682 Ullamcorper. Ave\",\r\n"
						+ "        \"address71\":\"9407 Quis, Rd.\",\r\n"
						+ "        \"address72\":\"P.O. Box 779, 7508 Nonummy Rd.\",\r\n"
						+ "        \"address73\":\"Ap #444-5797 At, Street\",\r\n"
						+ "        \"address74\":\"P.O. Box 155, 2299 A, Ave\",\r\n"
						+ "        \"address75\":\"P.O. Box 852, 6984 Egestas Rd.\",\r\n"
						+ "        \"address76\":\"526-1443 Eu St.\",\r\n"
						+ "        \"address77\":\"P.O. Box 229, 5436 Ac, Street\",\r\n"
						+ "        \"address78\":\"152-5421 Ac, Ave\",\r\n"
						+ "        \"address79\":\"Ap #307-7412 Lobortis Ave\",\r\n"
						+ "        \"address80\":\"Ap #910-472 Est, Road\",\r\n"
						+ "        \"address81\":\"P.O. Box 130, 4641 In St.\",\r\n"
						+ "        \"address82\":\"9572 Pellentesque St.\",\r\n"
						+ "        \"address83\":\"P.O. Box 222, 4876 Adipiscing Rd.\",\r\n"
						+ "        \"address84\":\"Ap #531-6101 Interdum St.\",\r\n"
						+ "        \"address85\":\"Ap #194-1553 Integer Street\",\r\n"
						+ "        \"address86\":\"Ap #299-1730 Turpis Av.\",\r\n"
						+ "        \"address87\":\"P.O. Box 445, 4034 Mollis Av.\",\r\n"
						+ "        \"address88\":\"Ap #966-7902 Dictum. Street\",\r\n"
						+ "        \"address89\":\"881-5020 Ante. Rd.\",\r\n"
						+ "        \"address90\":\"Ap #791-6728 Tortor, Av.\",\r\n"
						+ "        \"address91\":\"6219 Odio Avenue\",\r\n"
						+ "        \"address92\":\"909-2765 Adipiscing, Street\",\r\n"
						+ "        \"address93\":\"833-9194 Id, Rd.\",\r\n"
						+ "        \"address94\":\"356-1151 Semper. Avenue\",\r\n"
						+ "        \"address95\":\"731-6947 Dolor Av.\",\r\n"
						+ "        \"address96\":\"Ap #259-6445 Rhoncus. Rd.\",\r\n"
						+ "        \"address97\":\"610-844 Est Av.\",\r\n"
						+ "        \"address98\":\"Ap #596-402 Penatibus Rd.\",\r\n"
						+ "        \"address99\":\"Ap #587-1379 Dis Ave\",\r\n"
						+ "        \"address100\":\"Ap #981-6783 Magna. St.\"\r\n"
						+ "    }\r\n"
						+ "}")
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8")));
	}

}
