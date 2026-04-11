package com.soul.soa_additions.compat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Best-effort audit of Forge event subscribers — flags events with multiple listeners from
 * different mods so the user can spot priority fights manually. The Forge event bus does not
 * publish a clean "list all listeners" API, so we reflect into the bus internals; if the
 * field shape changes between Forge versions we just emit a note instead of crashing.
 */
final class EventListenerAudit {

    private static final Logger LOGGER = LoggerFactory.getLogger("SOA_EventAudit");

    private EventListenerAudit() {}

    static String buildReport() {
        StringBuilder md = new StringBuilder();
        md.append("Listing every event with **2+ listeners** so you can eyeball priority fights. ")
          .append("Listeners cancelling at HIGH/HIGHEST will silently break LOWER-priority listeners.\n\n");

        Map<String, List<ListenerEntry>> byEvent;
        try {
            byEvent = collectListeners(MinecraftForge.EVENT_BUS);
        } catch (Throwable t) {
            LOGGER.debug("Event bus reflection failed", t);
            md.append("_Event bus reflection unsupported on this Forge version — skipped._\n");
            return md.toString();
        }

        if (byEvent.isEmpty()) {
            md.append("_No multi-listener events detected._\n");
            return md.toString();
        }

        List<Map.Entry<String, List<ListenerEntry>>> sorted = new ArrayList<>(byEvent.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()));
        md.append("| event | listeners (priority) |\n|---|---|\n");
        for (var e : sorted) {
            if (e.getValue().size() < 2) continue;
            StringBuilder cell = new StringBuilder();
            for (ListenerEntry le : e.getValue()) {
                if (cell.length() > 0) cell.append("<br>");
                cell.append("`").append(le.owner).append("` @ ").append(le.priority);
            }
            md.append("| `").append(shortName(e.getKey())).append("` | ").append(cell).append(" |\n");
        }
        return md.toString();
    }

    private record ListenerEntry(String owner, EventPriority priority) {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<String, List<ListenerEntry>> collectListeners(IEventBus bus) throws Exception {
        // Forge's EventBus stores listeners in a Map<Class<?>, ListenerList>. We snoop into
        // that map, then ask each ListenerList for its priority-sorted handler arrays.
        Field listenersField = findField(bus.getClass(), "listeners");
        listenersField.setAccessible(true);
        Object map = listenersField.get(bus);
        Map<String, List<ListenerEntry>> out = new TreeMap<>();
        if (!(map instanceof Map<?, ?> raw)) return out;
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (!(entry.getKey() instanceof Class<?> evClass)) continue;
            Object listenerList = entry.getValue();
            if (listenerList == null) continue;
            List<ListenerEntry> entries = extractFromListenerList(listenerList);
            if (entries.size() >= 2) {
                long distinctOwners = entries.stream().map(ListenerEntry::owner).distinct().count();
                if (distinctOwners >= 2) {
                    out.put(evClass.getName(), entries);
                }
            }
        }
        return out;
    }

    private static List<ListenerEntry> extractFromListenerList(Object listenerList) {
        List<ListenerEntry> entries = new ArrayList<>();
        try {
            Field priorityField = findField(listenerList.getClass(), "priorities");
            if (priorityField == null) return entries;
            priorityField.setAccessible(true);
            Object priorityArray = priorityField.get(listenerList);
            if (!(priorityArray instanceof Object[] arr)) return entries;
            EventPriority[] order = EventPriority.values();
            for (int i = 0; i < arr.length && i < order.length; i++) {
                Object[] handlers = (Object[]) arr[i];
                if (handlers == null) continue;
                for (Object h : handlers) {
                    if (h == null) continue;
                    String owner = guessOwner(h);
                    entries.add(new ListenerEntry(owner, order[i]));
                }
            }
        } catch (Throwable ignored) {
            // ListenerList shape varies — silently degrade
        }
        entries.sort(Comparator.comparing(ListenerEntry::priority));
        return entries;
    }

    private static String guessOwner(Object handler) {
        // Forge wraps handlers in ASMEventHandler which exposes a target Method. We try a
        // couple of likely field names; failing that we just print the handler class name.
        for (String name : new String[]{"handler", "method", "subscriber"}) {
            try {
                Field f = findField(handler.getClass(), name);
                if (f == null) continue;
                f.setAccessible(true);
                Object val = f.get(handler);
                if (val == null) continue;
                if (val instanceof java.lang.reflect.Method m) {
                    return m.getDeclaringClass().getName();
                }
                return val.getClass().getName();
            } catch (Throwable ignored) {
            }
        }
        return handler.getClass().getName();
    }

    private static Field findField(Class<?> cls, String name) {
        Class<?> c = cls;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private static String shortName(String fqcn) {
        int dot = fqcn.lastIndexOf('.');
        return dot < 0 ? fqcn : fqcn.substring(dot + 1);
    }
}
