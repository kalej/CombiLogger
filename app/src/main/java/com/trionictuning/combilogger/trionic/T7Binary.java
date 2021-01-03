package com.trionictuning.combilogger.trionic;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.trionictuning.combilogger.CombiLoggerApp;

public class T7Binary {
    private List<Symbol> mSymbols;
    private HashMap<String, Symbol> mByName;

    public static T7Binary fromJSONFile(String name) {
        T7Binary result = new T7Binary();

        String json = null;
        try {
            InputStream is =
                    CombiLoggerApp.getInstance().getAssets().open(
                            String.format("binaries/%s.json", name
                            )
                    );
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            JSONArray array = new JSONArray(json);

            result.mSymbols = new LinkedList<>();
            result.mByName = new HashMap<>();

            for (int i = 0; i < array.length(); i++) {
                Symbol symbol = new Symbol(array.getJSONObject(i));

                result.mSymbols.add(symbol);
                result.mByName.put(symbol.getName(), symbol);
            }

            return result;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Symbol getSymbolByName(String name) {
        return mByName.get(name);
    }
}
