package de.amr.statemachine.core;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Map with boolean keys.
 * 
 * @author Armin Reichert
 *
 * @param <V> value type
 */
public class BooleanMap<V> implements Map<Boolean, V> {

	private final Entry<Boolean, V> f;
	private final Entry<Boolean, V> t;

	private Set<Boolean> keySet;

	public BooleanMap() {
		f = new AbstractMap.SimpleEntry<>(false, null);
		t = new AbstractMap.SimpleEntry<>(true, null);
	}

	@Override
	public int size() {
		if (f.getValue() == null && t.getValue() == null) {
			return 0;
		}
		if (f.getValue() != null && t.getValue() != null) {
			return 2;
		}
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}
		boolean booleanKey = (Boolean) key;
		return booleanKey ? t.getValue() != null : f.getValue() != null;
	}

	@Override
	public boolean containsValue(Object value) {
		Objects.requireNonNull(value);
		return value.equals(f.getValue()) || value.equals(t.getValue());
	}

	@Override
	public V get(Object key) {
		if (key == null) {
			return null;
		}
		boolean b = (Boolean) key;
		return b ? t.getValue() : f.getValue();
	}

	@Override
	public V put(Boolean key, V value) {
		V oldValue = get(key);
		if (key.booleanValue()) {
			t.setValue(value);
		} else {
			f.setValue(value);
		}
		return oldValue;
	}

	@Override
	public V remove(Object key) {
		V oldValue = get(key);
		boolean b = (Boolean) key;
		if (b) {
			t.setValue(null);
		} else {
			f.setValue(null);
		}
		return oldValue;
	}

	@Override
	public void putAll(Map<? extends Boolean, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		f.setValue(null);
		t.setValue(null);
	}

	@Override
	public Set<Boolean> keySet() {
		if (keySet == null) {
			keySet = new HashSet<>(2);
			if (f.getValue() != null)
				keySet.add(false);
			if (t.getValue() != null)
				keySet.add(true);
		}
		return keySet;
	}

	@Override
	public Set<Entry<Boolean, V>> entrySet() {
		Set<Entry<Boolean, V>> entrySet = new HashSet<>();
		if (f.getValue() != null)
			entrySet.add(f);
		if (t.getValue() != null)
			entrySet.add(t);
		return entrySet;
	}

	// TODO this is not a correct implementation of the Map interface contract
	@Override
	public Collection<V> values() {
		if (f.getValue() != null && t.getValue() != null) {
			return Arrays.asList(f.getValue(), t.getValue());
		}
		if (f.getValue() != null) {
			return Arrays.asList(f.getValue());
		}
		if (t.getValue() != null) {
			return Arrays.asList(t.getValue());
		}
		return Collections.emptyList();
	}
}
