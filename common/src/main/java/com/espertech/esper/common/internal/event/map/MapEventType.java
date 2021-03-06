/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.event.map;

import com.espertech.esper.common.client.EventPropertyDescriptor;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.meta.EventTypeMetadata;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeClassParameterized;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.collection.Pair;
import com.espertech.esper.common.internal.event.bean.service.BeanEventTypeFactory;
import com.espertech.esper.common.internal.event.core.*;
import com.espertech.esper.common.internal.event.property.IndexedProperty;
import com.espertech.esper.common.internal.event.property.MappedProperty;
import com.espertech.esper.common.internal.event.property.Property;
import com.espertech.esper.common.internal.event.property.PropertyParser;

import java.util.*;

/**
 * Implementation of the {@link EventType} interface for handling plain Maps containing name value pairs.
 */
public class MapEventType extends BaseNestableEventType {
    public final static EPTypeClass EPTYPE = new EPTypeClass(MapEventType.class);

    private static final EPTypeClass MAPEPTYPE = EPTypeClassParameterized.from(Map.class, String.class, Object.class);
    private static final EventTypeNestableGetterFactory GETTER_FACTORY = new EventTypeNestableGetterFactoryMap();

    protected Map<String, Pair<EventPropertyDescriptor, MapEventBeanPropertyWriter>> propertyWriters;
    protected EventPropertyDescriptor[] writablePropertyDescriptors;

    public MapEventType(EventTypeMetadata metadata,
                        Map<String, Object> propertyTypes,
                        EventType[] optionalSuperTypes,
                        Set<EventType> optionalDeepSupertypes,
                        String startTimestampPropertyName,
                        String endTimestampPropertyName,
                        BeanEventTypeFactory beanEventTypeFactory) {
        super(metadata, propertyTypes, optionalSuperTypes, optionalDeepSupertypes, startTimestampPropertyName, endTimestampPropertyName,
                GETTER_FACTORY, beanEventTypeFactory, false);
    }

    public final Class getUnderlyingType() {
        return Map.class;
    }

    public EPTypeClass getUnderlyingEPType() {
        return MAPEPTYPE;
    }

    public EventBeanCopyMethodForge getCopyMethodForge(String[] properties) {
        BaseNestableEventUtil.MapIndexedPropPair pair = BaseNestableEventUtil.getIndexedAndMappedProps(properties);

        if (pair.getMapProperties().isEmpty() && pair.getArrayProperties().isEmpty()) {
            return new MapEventBeanCopyMethodForge(this, beanEventTypeFactory.getEventBeanTypedEventFactory());
        } else {
            return new MapEventBeanCopyMethodWithArrayMapForge(this, beanEventTypeFactory.getEventBeanTypedEventFactory(), pair.getMapProperties(), pair.getArrayProperties());
        }
    }

    public Object getValue(String propertyName, Map values) {
        MapEventPropertyGetter getter = (MapEventPropertyGetter) getGetter(propertyName);
        return getter.getMap(values);
    }

    public MapEventBeanPropertyWriter getWriter(String propertyName) {
        if (writablePropertyDescriptors == null) {
            initializeWriters();
        }
        Pair<EventPropertyDescriptor, MapEventBeanPropertyWriter> pair = propertyWriters.get(propertyName);
        if (pair != null) {
            return pair.getSecond();
        }

        Property property = PropertyParser.parseAndWalkLaxToSimple(propertyName);
        if (property instanceof MappedProperty) {
            MappedProperty mapProp = (MappedProperty) property;
            if (!propertyItems.containsKey(mapProp.getPropertyNameAtomic())) {
                return null;
            }
            return new MapEventBeanPropertyWriterMapProp(mapProp.getPropertyNameAtomic(), mapProp.getKey());
        }

        if (property instanceof IndexedProperty) {
            IndexedProperty indexedProp = (IndexedProperty) property;
            if (!propertyItems.containsKey(indexedProp.getPropertyNameAtomic())) {
                return null;
            }
            return new MapEventBeanPropertyWriterIndexedProp(indexedProp.getPropertyNameAtomic(), indexedProp.getIndex());
        }

        return null;
    }

    public EventPropertyDescriptor getWritableProperty(String propertyName) {
        if (writablePropertyDescriptors == null) {
            initializeWriters();
        }
        Pair<EventPropertyDescriptor, ? extends EventPropertyWriter> pair = propertyWriters.get(propertyName);
        if (pair != null) {
            return pair.getFirst();
        }

        Property property = PropertyParser.parseAndWalkLaxToSimple(propertyName);
        if (property instanceof MappedProperty) {
            EventPropertyWriter writer = getWriter(propertyName);
            if (writer == null) {
                return null;
            }
            MappedProperty mapProp = (MappedProperty) property;
            return new EventPropertyDescriptor(mapProp.getPropertyNameAtomic(), EPTypePremade.OBJECT.getEPType(), false, true, false, true, false);
        }
        if (property instanceof IndexedProperty) {
            EventPropertyWriter writer = getWriter(propertyName);
            if (writer == null) {
                return null;
            }
            IndexedProperty indexedProp = (IndexedProperty) property;
            return new EventPropertyDescriptor(indexedProp.getPropertyNameAtomic(), EPTypePremade.OBJECT.getEPType(), true, false, true, false, false);
        }
        return null;
    }

    public EventPropertyDescriptor[] getWriteableProperties() {
        if (writablePropertyDescriptors == null) {
            initializeWriters();
        }
        return writablePropertyDescriptors;
    }

    public EventBeanWriter getWriter(String[] properties) {
        if (writablePropertyDescriptors == null) {
            initializeWriters();
        }

        boolean allSimpleProps = true;
        MapEventBeanPropertyWriter[] writers = new MapEventBeanPropertyWriter[properties.length];
        for (int i = 0; i < properties.length; i++) {
            Pair<EventPropertyDescriptor, MapEventBeanPropertyWriter> writerPair = propertyWriters.get(properties[i]);
            if (writerPair != null) {
                writers[i] = writerPair.getSecond();
            } else {
                writers[i] = getWriter(properties[i]);
                if (writers[i] == null) {
                    return null;
                }
                allSimpleProps = false;
            }
        }

        if (allSimpleProps) {
            return new MapEventBeanWriterSimpleProps(properties);
        } else {
            return new MapEventBeanWriterPerProp(writers);
        }
    }

    private void initializeWriters() {
        List<EventPropertyDescriptor> writeableProps = new ArrayList<EventPropertyDescriptor>();
        Map<String, Pair<EventPropertyDescriptor, MapEventBeanPropertyWriter>> propertWritersMap = new HashMap<String, Pair<EventPropertyDescriptor, MapEventBeanPropertyWriter>>();
        for (EventPropertyDescriptor prop : propertyDescriptors) {
            writeableProps.add(prop);
            final String propertyName = prop.getPropertyName();
            MapEventBeanPropertyWriter eventPropertyWriter = new MapEventBeanPropertyWriter(propertyName);
            propertWritersMap.put(propertyName, new Pair<EventPropertyDescriptor, MapEventBeanPropertyWriter>(prop, eventPropertyWriter));
        }

        propertyWriters = propertWritersMap;
        writablePropertyDescriptors = writeableProps.toArray(new EventPropertyDescriptor[writeableProps.size()]);
    }
}
