package de.amr.statemachine;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Map with boolean keys.
 * 
 * @author Armin Reichert
 *
 * @param <V> value type
 */
public class BooleanMap<V> implements Map<Boolean, V> {

	private V valueFalse, valueTrue;

	@Override
	public int size() {
		if (valueFalse != null) {
			return valueTrue != null ? 2 : 1;
		}
		return valueTrue != null ? 1 : 0;
	}

	@Override
	public boolean isEmpty() {
		return valueFalse == null && valueTrue == null;
	}

	@Override
	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}
		boolean booleanKey = (Boolean) key;
		return booleanKey ? valueTrue != null : valueFalse != null;
	}

	@Override
	public boolean containsValue(Object value) {
		if (value == null) {
			return false;
		}
		return value.equals(valueTrue) || value.equals(valueFalse);
	}

	@Override
	public V get(Object key) {
		if (key != null) {
			boolean booleanKey = (Boolean) key;
			return booleanKey ? valueTrue : valueFalse;
		}
		return null;
	}

	@Override
	public V put(Boolean key, V value) {
		V oldValue = get(key);
		if (key) {
			valueTrue = value;
		} else {
			valueFalse = value;
		}
		return oldValue;
	}

	@Override
	public V remove(Object key) {
		V oldValue = get(key);
		boolean booleanKey = (Boolean) key;
		if (booleanKey) {
			valueTrue = null;
		} else {
			valueFalse = null;
		}
		return oldValue;
	}

	@Override
	public void putAll(Map<? extends Boolean, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		valueFalse = valueTrue = null;
	}

	private Set<Boolean> keySet;

	@Override
	public Set<Boolean> keySet() {
		if (keySet == null) {
			keySet = new HashSet<Boolean>(2);
			keySet.add(true);
			keySet.add(false);
		}
		return keySet;
	}

	@Override
	public Set<Entry<Boolean, V>> entrySet() {
		Set<Entry<Boolean, V>> entrySet = new HashSet<Map.Entry<Boolean, V>>();
		entrySet.add(new AbstractMap.SimpleEntry<>(false, valueFalse));
		entrySet.add(new AbstractMap.SimpleEntry<>(true, valueTrue));
		return entrySet;
	}

	@Override
	public Collection<V> values() {
		return Arrays.asList(valueFalse, valueTrue);
	}
}
