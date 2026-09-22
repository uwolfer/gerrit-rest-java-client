/*
 * Copyright 2013-2026 Urs Wolfer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.urswolfer.gerrit.client.rest.http.common;

import com.google.common.truth.Truth;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;

/**
 * @author Thomas Forrer
 */
public class GerritAssert {

    private GerritAssert() {}

    /**
     * Asserts that {@code actual} and {@code expected} are the same kind of thing holding the same
     * values, however deeply nested - the Gerrit extension types do not implement {@code equals()}.
     *
     * <p>Both sides are written out by {@link #describe} and the two texts compared, so a mismatch
     * reads as a diff. What is compared:
     * <ul>
     *   <li>the class of every object, so a {@code CommentInfo} is not a {@code RobotCommentInfo}
     *       and an {@code Integer} is not a {@code Long};</li>
     *   <li>for collections and maps, which kind - list, set, sorted set, map or sorted map - but
     *       not which implementation, so a parsed {@code ArrayList} equals an expected
     *       {@code Arrays.asList};</li>
     *   <li>every non-static field, transient ones included;</li>
     *   <li>list, set and map order, and map keys with their types;</li>
     *   <li>{@code null}s, written out, including map values: a key mapped to {@code null} is not
     *       an absent key;</li>
     *   <li>timestamps to the nanosecond.</li>
     * </ul>
     *
     * <p>A value of a JDK type this does not know is an error rather than a guess: reflecting into
     * JDK classes is what made the XStream comparison this replaces fail on JDK 17+.
     */
    public static <T> void assertEquals(T actual, T expected) {
        Truth.assertThat(describe(actual)).isEqualTo(describe(expected));
    }

    static String describe(Object value) {
        StringBuilder out = new StringBuilder();
        new Describer(out).write(value, 0);
        return out.toString();
    }

    private static final class Describer {
        private final StringBuilder out;
        private final Map<Object, Boolean> inProgress = new IdentityHashMap<>();

        private Describer(StringBuilder out) {
            this.out = out;
        }

        void write(Object value, int depth) {
            if (value == null) {
                out.append("null");
            } else if (value instanceof String) {
                out.append('"').append(((String) value).replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            } else if (value instanceof Number || value instanceof Boolean || value instanceof Character) {
                out.append(value.getClass().getSimpleName()).append(' ').append(value);
            } else if (value instanceof Enum) {
                out.append(((Enum<?>) value).getDeclaringClass().getName()).append('.').append(((Enum<?>) value).name());
            } else if (value instanceof Timestamp) {
                out.append("Timestamp ").append(((Timestamp) value).toInstant());
            } else if (value instanceof Date) {
                out.append(value.getClass().getSimpleName()).append(' ').append(((Date) value).toInstant());
            } else if (value instanceof TemporalAccessor || value instanceof UUID) {
                out.append(value.getClass().getSimpleName()).append(' ').append(value);
            } else if (value instanceof Optional) {
                out.append("Optional ");
                write(((Optional<?>) value).orElse(null), depth);
            } else if (value instanceof Map) {
                nested(value, () -> writeMap((Map<?, ?>) value, depth));
            } else if (value instanceof Collection) {
                nested(value, () -> writeElements(collectionKind((Collection<?>) value), (Collection<?>) value, depth));
            } else if (value.getClass().isArray()) {
                nested(value, () -> writeElements("array", arrayElements(value), depth));
            } else if (isJdkType(value.getClass())) {
                throw new IllegalArgumentException("GerritAssert does not know how to compare "
                    + value.getClass().getName() + "; add a case for it to GerritAssert.Describer");
            } else {
                nested(value, () -> writeFields(value, depth));
            }
        }

        private void writeMap(Map<?, ?> map, int depth) {
            out.append(map instanceof SortedMap ? "sorted-map" : "map").append(" {");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                newline(depth + 1);
                write(entry.getKey(), depth + 1);
                out.append(": ");
                write(entry.getValue(), depth + 1);
            }
            close('}', depth, map.isEmpty());
        }

        private void writeElements(String kind, Collection<?> elements, int depth) {
            out.append(kind).append(" [");
            for (Object element : elements) {
                newline(depth + 1);
                write(element, depth + 1);
            }
            close(']', depth, elements.isEmpty());
        }

        private void writeFields(Object value, int depth) {
            List<Field> fields = fields(value.getClass());
            out.append(value.getClass().getName()).append(" {");
            for (Field field : fields) {
                newline(depth + 1);
                out.append(field.getName()).append(" = ");
                try {
                    field.setAccessible(true);
                    write(field.get(value), depth + 1);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
            close('}', depth, fields.isEmpty());
        }

        private void nested(Object value, Runnable writeBody) {
            if (inProgress.put(value, Boolean.TRUE) != null) {
                throw new IllegalArgumentException("cycle through " + value.getClass().getName());
            }
            try {
                writeBody.run();
            } finally {
                inProgress.remove(value);
            }
        }

        private void newline(int depth) {
            out.append('\n');
            for (int i = 0; i < depth; i++) {
                out.append("  ");
            }
        }

        private void close(char bracket, int depth, boolean empty) {
            if (!empty) {
                newline(depth);
            }
            out.append(bracket);
        }
    }

    private static String collectionKind(Collection<?> collection) {
        if (collection instanceof SortedSet) {
            return "sorted-set";
        }
        if (collection instanceof Set) {
            return "set";
        }
        if (collection instanceof List) {
            return "list";
        }
        return "collection";
    }

    private static List<Object> arrayElements(Object array) {
        List<Object> elements = new ArrayList<>();
        for (int i = 0; i < Array.getLength(array); i++) {
            elements.add(Array.get(array, i));
        }
        return elements;
    }

    /**
     * Superclass fields first, then the class's own, skipping statics and the compiler's synthetic
     * fields - such as the reference an anonymous class keeps to the test that created it.
     */
    private static List<Field> fields(Class<?> type) {
        if (type == null || type == Object.class) {
            return Collections.emptyList();
        }
        List<Field> fields = new ArrayList<>(fields(type.getSuperclass()));
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static boolean isJdkType(Class<?> type) {
        String name = type.getName();
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.")
            || name.startsWith("sun.") || name.startsWith("com.sun.");
    }
}
