
package logic;

import java.io.*;
import java.net.*;
import java.util.*;

public class APIhandle {

    private URL url;

    public APIhandle() throws MalformedURLException {
        this.url = new URL("https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql");
    }

    //tekee httprequestin apiin
    // dataoutputstream luodaan urlia varten
    //getQueryStopissa löytyy valmis hakurakenne, parametrina pysäkki
    //writeUTF lähettää pyynnön apiin
    //vastaus annetaan readFromStream-metodille, parametrina inputstream
    //metodi kutsuu parseGraphQL-metodia jolle viedään inputstream luettavaan muotoon muokattavaksi
    //joka palautetaan listana arrayta
    public List<String[]> makeHttpRequest(boolean stopQuery, String stop) throws IOException {
        List<String[]> jsonResponse = null;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        
        try {
            urlConnection = setConnection(urlConnection);

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

            String param = getParam(stopQuery, stop);

            endDataoutput(wr, param);

            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(stopQuery, inputStream);
        } catch (Exception e) {
        } finally {
            closeConnections(urlConnection, inputStream);
        }

        return jsonResponse;
    }

    public HttpURLConnection setConnection(HttpURLConnection urlConnection) throws IOException {
        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setRequestProperty("charset", "utf-8");
        urlConnection.setRequestProperty("Content-Type", "application/graphql");

        return urlConnection;
    }

    public void closeConnections(HttpURLConnection urlConnection, InputStream inputStream) throws IOException {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }

    public void endDataoutput(DataOutputStream wr, String param) throws IOException {
        wr.writeUTF(param);
        wr.flush();
        wr.close();
    }

    public String getParam(boolean stopQuery, String stop) throws IOException {
        String param = null;

        if (stopQuery) {
            param = getStops(stop);
        } else {
            param = getTrips(stop);
        }

        return param;
    }

    private String getTrips(String stop) throws IOException {
        String s = "{" +
                "                      stops(name: \"" + stop + "\") {" +
                "                        name" +
                "                          stoptimesWithoutPatterns {" +
                "                          trip {" +
                "                            routeShortName" +
                "                          }     " +
                "                          realtimeDeparture" +
                "                          departureDelay" +
                "                          realtimeState" +
                "                          headsign" +
                "                        }" +
                "                      }" +
                "                    }";
        return s;
    }

    private String getStops(String stop) throws IOException {
        String s = "{" +
                "                      stops(name:\"" + stop + "\") {" +
                "                        name" +
                "                          }" +
                "                           }";
        return s;
    }

    private List<String[]> readFromStream(boolean stopQuery, InputStream stream) throws IOException {
        Scanner s = new Scanner(stream).useDelimiter("},\\{").useDelimiter("name").useDelimiter("trip");

        List<String[]> tempList;

        if (stopQuery) {
            tempList = parseStopGraphQLResult(s);
        } else {
            tempList = parseGraphQLResult(s);
        }

        return tempList;
    }

    private List<String[]> parseStopGraphQLResult(Scanner s) {
        List<String[]> tempList = new ArrayList<>();

        while (s.hasNext()) {
            String[] tempSplit = s.next().split("name");

            for (int i = 1; i < tempSplit.length; i++) {
                tempSplit[i] = tempSplit[i].replaceAll("[^a-zäöåA-ZÖÄÅ0-9.' ']", "");
                String[] added = new String[1];
                added[0] = tempSplit[i];
                tempList.add(added);
            }
        }

        return tempList;
    }

    //[0]=linjakoodi, [1]=aika, [2]=delay, [3]=pävitetty?, [4]=mitä lukee siin
    private List<String[]> parseGraphQLResult(Scanner s) {
        List<String[]> tempList = new ArrayList<>();

        while (s.hasNext()) {

            String[] tempSplit = s.next().split("[:,]");

            for (int i = 0; i < tempSplit.length; i++) {
                if (i == 10) {
                    tempSplit[i] = tempSplit[i].replaceAll("[\"\\]\\}]", "");
                    continue;
                }

                tempSplit[i] = tempSplit[i].replaceAll("[^a-zäöåA-ZÖÄÅ0-9.' ']", "");
            }

            if (tempSplit.length < 10) {
                continue;
            }

            String[] finArr = {tempSplit[2], convertSeconds(tempSplit[4]), convertSeconds(tempSplit[6]), tempSplit[8], tempSplit[10]};

            tempList.add(finArr);
        }

        return tempList;
    }

    //sekunnit kellonajaksi
    public String convertSeconds(String s) {
        int totalSec = Integer.valueOf(s);

        if (totalSec >= 86400) {
            totalSec = totalSec - 86400;
        }

        int seconds = totalSec % 60;
        int hours = totalSec / 60;
        int minutes = hours % 60;
        hours = hours / 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
