package ca.fwe.weather.util;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by dewey on 2016-11-13.
 */

public class JSONSharedPreferences implements SharedPreferences {

    private JSONObject o;

    public JSONSharedPreferences(String json) {
        try {
            o = new JSONObject(json);
        } catch(JSONException e) {
            throw new IllegalArgumentException("invalid json: " + e.getMessage());
        }
    }

    public JSONSharedPreferences() {
        o = new JSONObject();
    }

    @Override
    public Map<String, ?> getAll() {
        Map<String, String> out = new HashMap<>();
        Iterator<String> keys = o.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                out.put(key, o.getString(key));
            } catch(JSONException e) {
                throw new RuntimeException("Bad JSON output in getAll()");
            }
        }
        return out;
    }

    @Nullable
    @Override
    public String getString(String key, String defValue) {
        try {
            return o.getString(key);
        } catch(JSONException e) {
            return defValue;
        }
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        try {
            Set<String> out = new HashSet<>();
            JSONArray a = o.getJSONArray(key);
            for(int i=0; i<a.length(); i++) {
                out.add(a.getString(i));
            }
            return out;
        } catch(JSONException e) {
            return defValues;
        }
    }

    @Override
    public int getInt(String key, int defValue) {
        try {
            return o.getInt(key);
        } catch(JSONException e) {
            return defValue;
        }
    }

    @Override
    public long getLong(String key, long defValue) {
        try {
            return o.getLong(key);
        } catch(JSONException e) {
            return defValue;
        }
    }

    @Override
    public float getFloat(String key, float defValue) {
        try {
            return Double.valueOf(o.getDouble(key)).floatValue();
        } catch(JSONException e) {
            return defValue;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        try {
            return o.getBoolean(key);
        } catch(JSONException e) {
            return defValue;
        }
    }

    @Override
    public boolean contains(String key) {
        return o.has(key);
    }

    @Override
    public Editor edit() {
        return new JSONEditor(o);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        throw new RuntimeException("not implemented");
    }

    private class JSONEditor implements SharedPreferences.Editor {


        JSONObject oEdit ;

        JSONEditor(JSONObject o) {
            try {
                oEdit = new JSONObject(o.toString());
            } catch(JSONException e) {
                throw new RuntimeException("Invalid JSON when creating JSONEditor");
            }
        }

        @Override
        public Editor putString(String key, String value) {
            try {
                oEdit.put(key, value);
            } catch(JSONException e) {
                throw new RuntimeException("JSON Exception on put");
            }
            return this;
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            try {
                JSONArray a = new JSONArray();
                for(String s: values) {
                    a.put(s);
                }
                oEdit.put(key, a);
            } catch(JSONException e) {
                throw new RuntimeException("JSON Exception on put");
            }
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            try {
                oEdit.put(key, value);
            } catch(JSONException e) {
                throw new RuntimeException("JSON Exception on put");
            }
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            try {
                oEdit.put(key, value);
            } catch(JSONException e) {
                throw new RuntimeException("JSON Exception on put");
            }
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            try {
                oEdit.put(key, value);
            } catch(JSONException e) {
                throw new RuntimeException("JSON Exception on put");
            }
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            try {
                oEdit.put(key, value);
            } catch(JSONException e) {
                throw new RuntimeException("JSON Exception on put");
            }
            return this;
        }

        @Override
        public Editor remove(String key) {
            oEdit.remove(key);
            return this;
        }

        @Override
        public Editor clear() {
            oEdit = new JSONObject();
            return this;
        }

        @Override
        public boolean commit() {
            o = oEdit;
            return true;
        }

        @Override
        public void apply() {
            this.commit();
        }
    }

}