package gmae.adventure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Action {

    private final ActionType type;
    private final Map<String, Object> params;

    public Action(ActionType type) {
        this.type = type;
        this.params = Collections.emptyMap();
    }

    public Action(ActionType type, Map<String, Object> params) {
        this.type = type;
        this.params = Collections.unmodifiableMap(new HashMap<>(params));
    }

    public ActionType getType() {
        return type;
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    public static Action of(ActionType type) {
        return new Action(type);
    }

    public static Action of(ActionType type, String key, Object value) {
        return new Action(type, Map.of(key, value));
    }

    public static Action of(ActionType type, Map<String, Object> params) {
        return new Action(type, params);
    }


    @Override
    public String toString() {
        if (params.isEmpty()) {
            return "Action(" + type + ")";
        } else {
            return "Action(" + type + ", " + params + ")";
        }
    }
}
