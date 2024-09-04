package com.yh04.joyfulmindapp.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomDateAdapter extends TypeAdapter<Date> {
    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(dateFormat1.format(value));
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String dateStr = in.nextString();
        try {
            return dateFormat1.parse(dateStr);
        } catch (ParseException e) {
            // dateFormat1 failed, try dateFormat2
            try {
                return dateFormat2.parse(dateStr);
            } catch (ParseException ex) {
                throw new IOException(ex);
            }
        }
    }
}
