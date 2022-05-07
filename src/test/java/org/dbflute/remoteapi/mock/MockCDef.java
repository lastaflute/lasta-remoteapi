/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.remoteapi.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationCodeType;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.dbflute.optional.OptionalThing;

/**
 * The definition of classification.
 * @author DBFlute(AutoGenerator)
 */
public interface MockCDef extends Classification {

    // for DBFlute-1.1.1
    String[] EMPTY_STRINGS = new String[] {};

    // for DBFlute-1.1.1
    public static class ClassificationNotFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ClassificationNotFoundException(String msg) {
            super(msg);
        }
    }

    /**
     * general boolean classification for every flg-column
     */
    public enum Flg implements MockCDef {
        /** Checked: means yes */
        True("1", "Checked", new String[] { "true" }),
        /** Unchecked: means no */
        False("0", "Unchecked", new String[] { "false" });
        private static final Map<String, Flg> _codeClsMap = new HashMap<String, Flg>();
        private static final Map<String, Flg> _nameClsMap = new HashMap<String, Flg>();
        static {
            for (Flg value : values()) {
                _codeClsMap.put(value.code().toLowerCase(), value);
                for (String sister : value.sisterSet()) {
                    _codeClsMap.put(sister.toLowerCase(), value);
                }
                _nameClsMap.put(value.name().toLowerCase(), value);
            }
        }
        private String _code;
        private String _alias;
        private Set<String> _sisterSet;

        private Flg(String code, String alias, String[] sisters) {
            _code = code;
            _alias = alias;
            _sisterSet = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(sisters)));
        }

        public String code() {
            return _code;
        }

        public String alias() {
            return _alias;
        }

        public Set<String> sisterSet() {
            return _sisterSet;
        }

        public Map<String, Object> subItemMap() {
            return Collections.emptyMap();
        }

        public ClassificationMeta meta() {
            return MockCDef.DefMeta.Flg;
        }

        public boolean inGroup(String groupName) {
            return false;
        }

        /**
         * Get the classification of the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
         * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<Flg> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new ClassificationNotFoundException("null code specified");
                });
            }
            if (code instanceof Flg) {
                return OptionalThing.of((Flg) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            return OptionalThing.ofNullable(_codeClsMap.get(code.toString().toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification code: " + code);
            });
        }

        /**
         * Find the classification by the name. (CaseInsensitive)
         * @param name The string of name, which is case-insensitive. (NotNull)
         * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<Flg> byName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The argument 'name' should not be null.");
            }
            return OptionalThing.ofNullable(_nameClsMap.get(name.toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification name: " + name);
            });
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use of(code).</span> <br>
         * Get the classification by the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the code. (NullAllowed: if not found, returns null)
         */
        public static Flg codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof Flg) {
                return (Flg) code;
            }
            return _codeClsMap.get(code.toString().toLowerCase());
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use byName(name).</span> <br>
         * Get the classification by the name (also called 'value' in ENUM world).
         * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the name. (NullAllowed: if not found, returns null)
         */
        public static Flg nameOf(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        /**
         * Get the list of all classification elements. (returns new copied list)
         * @return The snapshot list of all classification elements. (NotNull)
         */
        public static List<Flg> listAll() {
            return new ArrayList<Flg>(Arrays.asList(values()));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if not found, throws exception)
         */
        public static List<Flg> listByGroup(String groupName) {
            if (groupName == null) {
                throw new IllegalArgumentException("The argument 'groupName' should not be null.");
            }
            throw new ClassificationNotFoundException("Unknown classification group: Flg." + groupName);
        }

        /**
         * Get the list of classification elements corresponding to the specified codes. (returns new copied list) <br>
         * @param codeList The list of plain code, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the code list. (NotNull, EmptyAllowed: when empty specified)
         */
        public static List<Flg> listOf(Collection<String> codeList) {
            if (codeList == null) {
                throw new IllegalArgumentException("The argument 'codeList' should not be null.");
            }
            List<Flg> clsList = new ArrayList<Flg>(codeList.size());
            for (String code : codeList) {
                clsList.add(of(code).get());
            }
            return clsList;
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if the group is not found)
         */
        public static List<Flg> groupOf(String groupName) {
            return new ArrayList<Flg>(4);
        }

        @Override
        public String toString() {
            return code();
        }
    }

    /**
     * status of member from entry to withdrawal
     */
    public enum MemberStatus implements MockCDef {
        /** Formalized: as formal member, allowed to use all service */
        Formalized("FML", "Formalized", EMPTY_STRINGS),
        /** Withdrawal: withdrawal is fixed, not allowed to use service */
        Withdrawal("WDL", "Withdrawal", EMPTY_STRINGS),
        /** Provisional: first status after entry, allowed to use only part of service */
        Provisional("PRV", "Provisional", EMPTY_STRINGS);
        private static final Map<String, MemberStatus> _codeClsMap = new HashMap<String, MemberStatus>();
        private static final Map<String, MemberStatus> _nameClsMap = new HashMap<String, MemberStatus>();
        static {
            for (MemberStatus value : values()) {
                _codeClsMap.put(value.code().toLowerCase(), value);
                for (String sister : value.sisterSet()) {
                    _codeClsMap.put(sister.toLowerCase(), value);
                }
                _nameClsMap.put(value.name().toLowerCase(), value);
            }
        }
        private String _code;
        private String _alias;
        private Set<String> _sisterSet;

        private MemberStatus(String code, String alias, String[] sisters) {
            _code = code;
            _alias = alias;
            _sisterSet = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(sisters)));
        }

        public String code() {
            return _code;
        }

        public String alias() {
            return _alias;
        }

        public Set<String> sisterSet() {
            return _sisterSet;
        }

        public Map<String, Object> subItemMap() {
            return Collections.emptyMap();
        }

        public ClassificationMeta meta() {
            return MockCDef.DefMeta.MemberStatus;
        }

        /**
         * Is the classification in the group? <br>
         * means member that can use services <br>
         * The group elements:[Formalized, Provisional]
         * @return The determination, true or false.
         */
        public boolean isServiceAvailable() {
            return Formalized.equals(this) || Provisional.equals(this);
        }

        /**
         * Is the classification in the group? <br>
         * Members are not formalized yet <br>
         * The group elements:[Provisional]
         * @return The determination, true or false.
         */
        public boolean isShortOfFormalized() {
            return Provisional.equals(this);
        }

        public boolean inGroup(String groupName) {
            if ("serviceAvailable".equals(groupName)) {
                return isServiceAvailable();
            }
            if ("shortOfFormalized".equals(groupName)) {
                return isShortOfFormalized();
            }
            return false;
        }

        /**
         * Get the classification of the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
         * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<MemberStatus> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new ClassificationNotFoundException("null code specified");
                });
            }
            if (code instanceof MemberStatus) {
                return OptionalThing.of((MemberStatus) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            return OptionalThing.ofNullable(_codeClsMap.get(code.toString().toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification code: " + code);
            });
        }

        /**
         * Find the classification by the name. (CaseInsensitive)
         * @param name The string of name, which is case-insensitive. (NotNull)
         * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<MemberStatus> byName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The argument 'name' should not be null.");
            }
            return OptionalThing.ofNullable(_nameClsMap.get(name.toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification name: " + name);
            });
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use of(code).</span> <br>
         * Get the classification by the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the code. (NullAllowed: if not found, returns null)
         */
        public static MemberStatus codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof MemberStatus) {
                return (MemberStatus) code;
            }
            return _codeClsMap.get(code.toString().toLowerCase());
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use byName(name).</span> <br>
         * Get the classification by the name (also called 'value' in ENUM world).
         * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the name. (NullAllowed: if not found, returns null)
         */
        public static MemberStatus nameOf(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        /**
         * Get the list of all classification elements. (returns new copied list)
         * @return The snapshot list of all classification elements. (NotNull)
         */
        public static List<MemberStatus> listAll() {
            return new ArrayList<MemberStatus>(Arrays.asList(values()));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if not found, throws exception)
         */
        public static List<MemberStatus> listByGroup(String groupName) {
            if (groupName == null) {
                throw new IllegalArgumentException("The argument 'groupName' should not be null.");
            }
            if ("serviceAvailable".equalsIgnoreCase(groupName)) {
                return listOfServiceAvailable();
            }
            if ("shortOfFormalized".equalsIgnoreCase(groupName)) {
                return listOfShortOfFormalized();
            }
            throw new ClassificationNotFoundException("Unknown classification group: MemberStatus." + groupName);
        }

        /**
         * Get the list of classification elements corresponding to the specified codes. (returns new copied list) <br>
         * @param codeList The list of plain code, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the code list. (NotNull, EmptyAllowed: when empty specified)
         */
        public static List<MemberStatus> listOf(Collection<String> codeList) {
            if (codeList == null) {
                throw new IllegalArgumentException("The argument 'codeList' should not be null.");
            }
            List<MemberStatus> clsList = new ArrayList<MemberStatus>(codeList.size());
            for (String code : codeList) {
                clsList.add(of(code).get());
            }
            return clsList;
        }

        /**
         * Get the list of group classification elements. (returns new copied list) <br>
         * means member that can use services <br>
         * The group elements:[Formalized, Provisional]
         * @return The snapshot list of classification elements in the group. (NotNull)
         */
        public static List<MemberStatus> listOfServiceAvailable() {
            return new ArrayList<MemberStatus>(Arrays.asList(Formalized, Provisional));
        }

        /**
         * Get the list of group classification elements. (returns new copied list) <br>
         * Members are not formalized yet <br>
         * The group elements:[Provisional]
         * @return The snapshot list of classification elements in the group. (NotNull)
         */
        public static List<MemberStatus> listOfShortOfFormalized() {
            return new ArrayList<MemberStatus>(Arrays.asList(Provisional));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if the group is not found)
         */
        public static List<MemberStatus> groupOf(String groupName) {
            if ("serviceAvailable".equals(groupName)) {
                return listOfServiceAvailable();
            }
            if ("shortOfFormalized".equals(groupName)) {
                return listOfShortOfFormalized();
            }
            return new ArrayList<MemberStatus>(4);
        }

        @Override
        public String toString() {
            return code();
        }
    }

    /**
     * rank of service member gets
     */
    public enum ServiceRank implements MockCDef {
        /** PLATINUM: platinum rank */
        Platinum("PLT", "PLATINUM", EMPTY_STRINGS),
        /** GOLD: gold rank */
        Gold("GLD", "GOLD", EMPTY_STRINGS),
        /** SILVER: silver rank */
        Silver("SIL", "SILVER", EMPTY_STRINGS),
        /** BRONZE: bronze rank */
        Bronze("BRZ", "BRONZE", EMPTY_STRINGS),
        /** PLASTIC: plastic rank */
        Plastic("PLS", "PLASTIC", EMPTY_STRINGS);
        private static final Map<String, ServiceRank> _codeClsMap = new HashMap<String, ServiceRank>();
        private static final Map<String, ServiceRank> _nameClsMap = new HashMap<String, ServiceRank>();
        static {
            for (ServiceRank value : values()) {
                _codeClsMap.put(value.code().toLowerCase(), value);
                for (String sister : value.sisterSet()) {
                    _codeClsMap.put(sister.toLowerCase(), value);
                }
                _nameClsMap.put(value.name().toLowerCase(), value);
            }
        }
        private String _code;
        private String _alias;
        private Set<String> _sisterSet;

        private ServiceRank(String code, String alias, String[] sisters) {
            _code = code;
            _alias = alias;
            _sisterSet = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(sisters)));
        }

        public String code() {
            return _code;
        }

        public String alias() {
            return _alias;
        }

        public Set<String> sisterSet() {
            return _sisterSet;
        }

        public Map<String, Object> subItemMap() {
            return Collections.emptyMap();
        }

        public ClassificationMeta meta() {
            return MockCDef.DefMeta.ServiceRank;
        }

        public boolean inGroup(String groupName) {
            return false;
        }

        /**
         * Get the classification of the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
         * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<ServiceRank> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new ClassificationNotFoundException("null code specified");
                });
            }
            if (code instanceof ServiceRank) {
                return OptionalThing.of((ServiceRank) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            return OptionalThing.ofNullable(_codeClsMap.get(code.toString().toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification code: " + code);
            });
        }

        /**
         * Find the classification by the name. (CaseInsensitive)
         * @param name The string of name, which is case-insensitive. (NotNull)
         * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<ServiceRank> byName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The argument 'name' should not be null.");
            }
            return OptionalThing.ofNullable(_nameClsMap.get(name.toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification name: " + name);
            });
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use of(code).</span> <br>
         * Get the classification by the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the code. (NullAllowed: if not found, returns null)
         */
        public static ServiceRank codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof ServiceRank) {
                return (ServiceRank) code;
            }
            return _codeClsMap.get(code.toString().toLowerCase());
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use byName(name).</span> <br>
         * Get the classification by the name (also called 'value' in ENUM world).
         * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the name. (NullAllowed: if not found, returns null)
         */
        public static ServiceRank nameOf(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        /**
         * Get the list of all classification elements. (returns new copied list)
         * @return The snapshot list of all classification elements. (NotNull)
         */
        public static List<ServiceRank> listAll() {
            return new ArrayList<ServiceRank>(Arrays.asList(values()));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if not found, throws exception)
         */
        public static List<ServiceRank> listByGroup(String groupName) {
            if (groupName == null) {
                throw new IllegalArgumentException("The argument 'groupName' should not be null.");
            }
            throw new ClassificationNotFoundException("Unknown classification group: ServiceRank." + groupName);
        }

        /**
         * Get the list of classification elements corresponding to the specified codes. (returns new copied list) <br>
         * @param codeList The list of plain code, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the code list. (NotNull, EmptyAllowed: when empty specified)
         */
        public static List<ServiceRank> listOf(Collection<String> codeList) {
            if (codeList == null) {
                throw new IllegalArgumentException("The argument 'codeList' should not be null.");
            }
            List<ServiceRank> clsList = new ArrayList<ServiceRank>(codeList.size());
            for (String code : codeList) {
                clsList.add(of(code).get());
            }
            return clsList;
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if the group is not found)
         */
        public static List<ServiceRank> groupOf(String groupName) {
            return new ArrayList<ServiceRank>(4);
        }

        @Override
        public String toString() {
            return code();
        }
    }

    /**
     * mainly region of member address
     */
    public enum Region implements MockCDef {
        /** AMERICA */
        America("1", "AMERICA", EMPTY_STRINGS),
        /** CANADA */
        Canada("2", "CANADA", EMPTY_STRINGS),
        /** CHINA */
        China("3", "CHINA", EMPTY_STRINGS),
        /** CHIBA */
        Chiba("4", "CHIBA", EMPTY_STRINGS);
        private static final Map<String, Region> _codeClsMap = new HashMap<String, Region>();
        private static final Map<String, Region> _nameClsMap = new HashMap<String, Region>();
        static {
            for (Region value : values()) {
                _codeClsMap.put(value.code().toLowerCase(), value);
                for (String sister : value.sisterSet()) {
                    _codeClsMap.put(sister.toLowerCase(), value);
                }
                _nameClsMap.put(value.name().toLowerCase(), value);
            }
        }
        private String _code;
        private String _alias;
        private Set<String> _sisterSet;

        private Region(String code, String alias, String[] sisters) {
            _code = code;
            _alias = alias;
            _sisterSet = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(sisters)));
        }

        public String code() {
            return _code;
        }

        public String alias() {
            return _alias;
        }

        public Set<String> sisterSet() {
            return _sisterSet;
        }

        public Map<String, Object> subItemMap() {
            return Collections.emptyMap();
        }

        public ClassificationMeta meta() {
            return MockCDef.DefMeta.Region;
        }

        public boolean inGroup(String groupName) {
            return false;
        }

        /**
         * Get the classification of the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
         * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<Region> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new ClassificationNotFoundException("null code specified");
                });
            }
            if (code instanceof Region) {
                return OptionalThing.of((Region) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            return OptionalThing.ofNullable(_codeClsMap.get(code.toString().toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification code: " + code);
            });
        }

        /**
         * Find the classification by the name. (CaseInsensitive)
         * @param name The string of name, which is case-insensitive. (NotNull)
         * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<Region> byName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The argument 'name' should not be null.");
            }
            return OptionalThing.ofNullable(_nameClsMap.get(name.toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification name: " + name);
            });
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use of(code).</span> <br>
         * Get the classification by the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the code. (NullAllowed: if not found, returns null)
         */
        public static Region codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof Region) {
                return (Region) code;
            }
            return _codeClsMap.get(code.toString().toLowerCase());
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use byName(name).</span> <br>
         * Get the classification by the name (also called 'value' in ENUM world).
         * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the name. (NullAllowed: if not found, returns null)
         */
        public static Region nameOf(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        /**
         * Get the list of all classification elements. (returns new copied list)
         * @return The snapshot list of all classification elements. (NotNull)
         */
        public static List<Region> listAll() {
            return new ArrayList<Region>(Arrays.asList(values()));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if not found, throws exception)
         */
        public static List<Region> listByGroup(String groupName) {
            if (groupName == null) {
                throw new IllegalArgumentException("The argument 'groupName' should not be null.");
            }
            throw new ClassificationNotFoundException("Unknown classification group: Region." + groupName);
        }

        /**
         * Get the list of classification elements corresponding to the specified codes. (returns new copied list) <br>
         * @param codeList The list of plain code, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the code list. (NotNull, EmptyAllowed: when empty specified)
         */
        public static List<Region> listOf(Collection<String> codeList) {
            if (codeList == null) {
                throw new IllegalArgumentException("The argument 'codeList' should not be null.");
            }
            List<Region> clsList = new ArrayList<Region>(codeList.size());
            for (String code : codeList) {
                clsList.add(of(code).get());
            }
            return clsList;
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if the group is not found)
         */
        public static List<Region> groupOf(String groupName) {
            return new ArrayList<Region>(4);
        }

        @Override
        public String toString() {
            return code();
        }
    }

    /**
     * reason for member withdrawal
     */
    public enum WithdrawalReason implements MockCDef {
        /** SIT: site is not kindness */
        Sit("SIT", "SIT", EMPTY_STRINGS),
        /** PRD: no attractive product */
        Prd("PRD", "PRD", EMPTY_STRINGS),
        /** FRT: because of furiten */
        Frt("FRT", "FRT", EMPTY_STRINGS),
        /** OTH: other reasons */
        Oth("OTH", "OTH", EMPTY_STRINGS);
        private static final Map<String, WithdrawalReason> _codeClsMap = new HashMap<String, WithdrawalReason>();
        private static final Map<String, WithdrawalReason> _nameClsMap = new HashMap<String, WithdrawalReason>();
        static {
            for (WithdrawalReason value : values()) {
                _codeClsMap.put(value.code().toLowerCase(), value);
                for (String sister : value.sisterSet()) {
                    _codeClsMap.put(sister.toLowerCase(), value);
                }
                _nameClsMap.put(value.name().toLowerCase(), value);
            }
        }
        private String _code;
        private String _alias;
        private Set<String> _sisterSet;

        private WithdrawalReason(String code, String alias, String[] sisters) {
            _code = code;
            _alias = alias;
            _sisterSet = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(sisters)));
        }

        public String code() {
            return _code;
        }

        public String alias() {
            return _alias;
        }

        public Set<String> sisterSet() {
            return _sisterSet;
        }

        public Map<String, Object> subItemMap() {
            return Collections.emptyMap();
        }

        public ClassificationMeta meta() {
            return MockCDef.DefMeta.WithdrawalReason;
        }

        public boolean inGroup(String groupName) {
            return false;
        }

        /**
         * Get the classification of the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
         * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<WithdrawalReason> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new ClassificationNotFoundException("null code specified");
                });
            }
            if (code instanceof WithdrawalReason) {
                return OptionalThing.of((WithdrawalReason) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            return OptionalThing.ofNullable(_codeClsMap.get(code.toString().toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification code: " + code);
            });
        }

        /**
         * Find the classification by the name. (CaseInsensitive)
         * @param name The string of name, which is case-insensitive. (NotNull)
         * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<WithdrawalReason> byName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The argument 'name' should not be null.");
            }
            return OptionalThing.ofNullable(_nameClsMap.get(name.toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification name: " + name);
            });
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use of(code).</span> <br>
         * Get the classification by the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the code. (NullAllowed: if not found, returns null)
         */
        public static WithdrawalReason codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof WithdrawalReason) {
                return (WithdrawalReason) code;
            }
            return _codeClsMap.get(code.toString().toLowerCase());
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use byName(name).</span> <br>
         * Get the classification by the name (also called 'value' in ENUM world).
         * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the name. (NullAllowed: if not found, returns null)
         */
        public static WithdrawalReason nameOf(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        /**
         * Get the list of all classification elements. (returns new copied list)
         * @return The snapshot list of all classification elements. (NotNull)
         */
        public static List<WithdrawalReason> listAll() {
            return new ArrayList<WithdrawalReason>(Arrays.asList(values()));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if not found, throws exception)
         */
        public static List<WithdrawalReason> listByGroup(String groupName) {
            if (groupName == null) {
                throw new IllegalArgumentException("The argument 'groupName' should not be null.");
            }
            throw new ClassificationNotFoundException("Unknown classification group: WithdrawalReason." + groupName);
        }

        /**
         * Get the list of classification elements corresponding to the specified codes. (returns new copied list) <br>
         * @param codeList The list of plain code, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the code list. (NotNull, EmptyAllowed: when empty specified)
         */
        public static List<WithdrawalReason> listOf(Collection<String> codeList) {
            if (codeList == null) {
                throw new IllegalArgumentException("The argument 'codeList' should not be null.");
            }
            List<WithdrawalReason> clsList = new ArrayList<WithdrawalReason>(codeList.size());
            for (String code : codeList) {
                clsList.add(of(code).get());
            }
            return clsList;
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if the group is not found)
         */
        public static List<WithdrawalReason> groupOf(String groupName) {
            return new ArrayList<WithdrawalReason>(4);
        }

        @Override
        public String toString() {
            return code();
        }
    }

    /**
     * category of product. self reference
     */
    public enum ProductCategory implements MockCDef {
        /** Music */
        Music("MSC", "Music", EMPTY_STRINGS),
        /** Food */
        Food("FOD", "Food", EMPTY_STRINGS),
        /** Herb: of Food */
        Herb("HEB", "Herb", EMPTY_STRINGS),
        /** MusicCD: of Music */
        MusicCD("MCD", "MusicCD", EMPTY_STRINGS),
        /** Instruments: of Music */
        Instruments("INS", "Instruments", EMPTY_STRINGS);
        private static final Map<String, ProductCategory> _codeClsMap = new HashMap<String, ProductCategory>();
        private static final Map<String, ProductCategory> _nameClsMap = new HashMap<String, ProductCategory>();
        static {
            for (ProductCategory value : values()) {
                _codeClsMap.put(value.code().toLowerCase(), value);
                for (String sister : value.sisterSet()) {
                    _codeClsMap.put(sister.toLowerCase(), value);
                }
                _nameClsMap.put(value.name().toLowerCase(), value);
            }
        }
        private String _code;
        private String _alias;
        private Set<String> _sisterSet;

        private ProductCategory(String code, String alias, String[] sisters) {
            _code = code;
            _alias = alias;
            _sisterSet = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(sisters)));
        }

        public String code() {
            return _code;
        }

        public String alias() {
            return _alias;
        }

        public Set<String> sisterSet() {
            return _sisterSet;
        }

        public Map<String, Object> subItemMap() {
            return Collections.emptyMap();
        }

        public ClassificationMeta meta() {
            return MockCDef.DefMeta.ProductCategory;
        }

        public boolean inGroup(String groupName) {
            return false;
        }

        /**
         * Get the classification of the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
         * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<ProductCategory> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new ClassificationNotFoundException("null code specified");
                });
            }
            if (code instanceof ProductCategory) {
                return OptionalThing.of((ProductCategory) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            return OptionalThing.ofNullable(_codeClsMap.get(code.toString().toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification code: " + code);
            });
        }

        /**
         * Find the classification by the name. (CaseInsensitive)
         * @param name The string of name, which is case-insensitive. (NotNull)
         * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<ProductCategory> byName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The argument 'name' should not be null.");
            }
            return OptionalThing.ofNullable(_nameClsMap.get(name.toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification name: " + name);
            });
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use of(code).</span> <br>
         * Get the classification by the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the code. (NullAllowed: if not found, returns null)
         */
        public static ProductCategory codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof ProductCategory) {
                return (ProductCategory) code;
            }
            return _codeClsMap.get(code.toString().toLowerCase());
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use byName(name).</span> <br>
         * Get the classification by the name (also called 'value' in ENUM world).
         * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the name. (NullAllowed: if not found, returns null)
         */
        public static ProductCategory nameOf(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        /**
         * Get the list of all classification elements. (returns new copied list)
         * @return The snapshot list of all classification elements. (NotNull)
         */
        public static List<ProductCategory> listAll() {
            return new ArrayList<ProductCategory>(Arrays.asList(values()));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if not found, throws exception)
         */
        public static List<ProductCategory> listByGroup(String groupName) {
            if (groupName == null) {
                throw new IllegalArgumentException("The argument 'groupName' should not be null.");
            }
            throw new ClassificationNotFoundException("Unknown classification group: ProductCategory." + groupName);
        }

        /**
         * Get the list of classification elements corresponding to the specified codes. (returns new copied list) <br>
         * @param codeList The list of plain code, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the code list. (NotNull, EmptyAllowed: when empty specified)
         */
        public static List<ProductCategory> listOf(Collection<String> codeList) {
            if (codeList == null) {
                throw new IllegalArgumentException("The argument 'codeList' should not be null.");
            }
            List<ProductCategory> clsList = new ArrayList<ProductCategory>(codeList.size());
            for (String code : codeList) {
                clsList.add(of(code).get());
            }
            return clsList;
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if the group is not found)
         */
        public static List<ProductCategory> groupOf(String groupName) {
            return new ArrayList<ProductCategory>(4);
        }

        @Override
        public String toString() {
            return code();
        }
    }

    /**
     * status for product
     */
    public enum ProductStatus implements MockCDef {
        /** OnSaleProduction */
        OnSaleProduction("ONS", "OnSaleProduction", EMPTY_STRINGS),
        /** ProductionStop */
        ProductionStop("PST", "ProductionStop", EMPTY_STRINGS),
        /** SaleStop */
        SaleStop("SST", "SaleStop", EMPTY_STRINGS);
        private static final Map<String, ProductStatus> _codeClsMap = new HashMap<String, ProductStatus>();
        private static final Map<String, ProductStatus> _nameClsMap = new HashMap<String, ProductStatus>();
        static {
            for (ProductStatus value : values()) {
                _codeClsMap.put(value.code().toLowerCase(), value);
                for (String sister : value.sisterSet()) {
                    _codeClsMap.put(sister.toLowerCase(), value);
                }
                _nameClsMap.put(value.name().toLowerCase(), value);
            }
        }
        private String _code;
        private String _alias;
        private Set<String> _sisterSet;

        private ProductStatus(String code, String alias, String[] sisters) {
            _code = code;
            _alias = alias;
            _sisterSet = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(sisters)));
        }

        public String code() {
            return _code;
        }

        public String alias() {
            return _alias;
        }

        public Set<String> sisterSet() {
            return _sisterSet;
        }

        public Map<String, Object> subItemMap() {
            return Collections.emptyMap();
        }

        public ClassificationMeta meta() {
            return MockCDef.DefMeta.ProductStatus;
        }

        public boolean inGroup(String groupName) {
            return false;
        }

        /**
         * Get the classification of the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
         * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<ProductStatus> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new ClassificationNotFoundException("null code specified");
                });
            }
            if (code instanceof ProductStatus) {
                return OptionalThing.of((ProductStatus) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            return OptionalThing.ofNullable(_codeClsMap.get(code.toString().toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification code: " + code);
            });
        }

        /**
         * Find the classification by the name. (CaseInsensitive)
         * @param name The string of name, which is case-insensitive. (NotNull)
         * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<ProductStatus> byName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The argument 'name' should not be null.");
            }
            return OptionalThing.ofNullable(_nameClsMap.get(name.toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification name: " + name);
            });
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use of(code).</span> <br>
         * Get the classification by the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the code. (NullAllowed: if not found, returns null)
         */
        public static ProductStatus codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof ProductStatus) {
                return (ProductStatus) code;
            }
            return _codeClsMap.get(code.toString().toLowerCase());
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use byName(name).</span> <br>
         * Get the classification by the name (also called 'value' in ENUM world).
         * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the name. (NullAllowed: if not found, returns null)
         */
        public static ProductStatus nameOf(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        /**
         * Get the list of all classification elements. (returns new copied list)
         * @return The snapshot list of all classification elements. (NotNull)
         */
        public static List<ProductStatus> listAll() {
            return new ArrayList<ProductStatus>(Arrays.asList(values()));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if not found, throws exception)
         */
        public static List<ProductStatus> listByGroup(String groupName) {
            if (groupName == null) {
                throw new IllegalArgumentException("The argument 'groupName' should not be null.");
            }
            throw new ClassificationNotFoundException("Unknown classification group: ProductStatus." + groupName);
        }

        /**
         * Get the list of classification elements corresponding to the specified codes. (returns new copied list) <br>
         * @param codeList The list of plain code, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the code list. (NotNull, EmptyAllowed: when empty specified)
         */
        public static List<ProductStatus> listOf(Collection<String> codeList) {
            if (codeList == null) {
                throw new IllegalArgumentException("The argument 'codeList' should not be null.");
            }
            List<ProductStatus> clsList = new ArrayList<ProductStatus>(codeList.size());
            for (String code : codeList) {
                clsList.add(of(code).get());
            }
            return clsList;
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if the group is not found)
         */
        public static List<ProductStatus> groupOf(String groupName) {
            return new ArrayList<ProductStatus>(4);
        }

        @Override
        public String toString() {
            return code();
        }
    }

    /**
     * method of payment for purchase
     */
    public enum PaymentMethod implements MockCDef {
        /** by hand: payment by hand, face-to-face */
        ByHand("HAN", "by hand", EMPTY_STRINGS),
        /** bank transfer: bank transfer payment */
        BankTransfer("BAK", "bank transfer", EMPTY_STRINGS),
        /** credit card: credit card payment */
        CreditCard("CRC", "credit card", EMPTY_STRINGS);
        private static final Map<String, PaymentMethod> _codeClsMap = new HashMap<String, PaymentMethod>();
        private static final Map<String, PaymentMethod> _nameClsMap = new HashMap<String, PaymentMethod>();
        static {
            for (PaymentMethod value : values()) {
                _codeClsMap.put(value.code().toLowerCase(), value);
                for (String sister : value.sisterSet()) {
                    _codeClsMap.put(sister.toLowerCase(), value);
                }
                _nameClsMap.put(value.name().toLowerCase(), value);
            }
        }
        private String _code;
        private String _alias;
        private Set<String> _sisterSet;

        private PaymentMethod(String code, String alias, String[] sisters) {
            _code = code;
            _alias = alias;
            _sisterSet = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList(sisters)));
        }

        public String code() {
            return _code;
        }

        public String alias() {
            return _alias;
        }

        public Set<String> sisterSet() {
            return _sisterSet;
        }

        public Map<String, Object> subItemMap() {
            return Collections.emptyMap();
        }

        public ClassificationMeta meta() {
            return MockCDef.DefMeta.PaymentMethod;
        }

        /**
         * Is the classification in the group? <br>
         * the most recommended method <br>
         * The group elements:[ByHand]
         * @return The determination, true or false.
         */
        public boolean isRecommended() {
            return ByHand.equals(this);
        }

        public boolean inGroup(String groupName) {
            if ("recommended".equals(groupName)) {
                return isRecommended();
            }
            return false;
        }

        /**
         * Get the classification of the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
         * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<PaymentMethod> of(Object code) {
            if (code == null) {
                return OptionalThing.ofNullable(null, () -> {
                    throw new ClassificationNotFoundException("null code specified");
                });
            }
            if (code instanceof PaymentMethod) {
                return OptionalThing.of((PaymentMethod) code);
            }
            if (code instanceof OptionalThing<?>) {
                return of(((OptionalThing<?>) code).orElse(null));
            }
            return OptionalThing.ofNullable(_codeClsMap.get(code.toString().toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification code: " + code);
            });
        }

        /**
         * Find the classification by the name. (CaseInsensitive)
         * @param name The string of name, which is case-insensitive. (NotNull)
         * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
         */
        public static OptionalThing<PaymentMethod> byName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The argument 'name' should not be null.");
            }
            return OptionalThing.ofNullable(_nameClsMap.get(name.toLowerCase()), () -> {
                throw new ClassificationNotFoundException("Unknown classification name: " + name);
            });
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use of(code).</span> <br>
         * Get the classification by the code. (CaseInsensitive)
         * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the code. (NullAllowed: if not found, returns null)
         */
        public static PaymentMethod codeOf(Object code) {
            if (code == null) {
                return null;
            }
            if (code instanceof PaymentMethod) {
                return (PaymentMethod) code;
            }
            return _codeClsMap.get(code.toString().toLowerCase());
        }

        /**
         * <span style="color: #AD4747; font-size: 120%">Old style so use byName(name).</span> <br>
         * Get the classification by the name (also called 'value' in ENUM world).
         * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
         * @return The instance of the corresponding classification to the name. (NullAllowed: if not found, returns null)
         */
        public static PaymentMethod nameOf(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        /**
         * Get the list of all classification elements. (returns new copied list)
         * @return The snapshot list of all classification elements. (NotNull)
         */
        public static List<PaymentMethod> listAll() {
            return new ArrayList<PaymentMethod>(Arrays.asList(values()));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if not found, throws exception)
         */
        public static List<PaymentMethod> listByGroup(String groupName) {
            if (groupName == null) {
                throw new IllegalArgumentException("The argument 'groupName' should not be null.");
            }
            if ("recommended".equalsIgnoreCase(groupName)) {
                return listOfRecommended();
            }
            throw new ClassificationNotFoundException("Unknown classification group: PaymentMethod." + groupName);
        }

        /**
         * Get the list of classification elements corresponding to the specified codes. (returns new copied list) <br>
         * @param codeList The list of plain code, which is case-insensitive. (NotNull)
         * @return The snapshot list of classification elements in the code list. (NotNull, EmptyAllowed: when empty specified)
         */
        public static List<PaymentMethod> listOf(Collection<String> codeList) {
            if (codeList == null) {
                throw new IllegalArgumentException("The argument 'codeList' should not be null.");
            }
            List<PaymentMethod> clsList = new ArrayList<PaymentMethod>(codeList.size());
            for (String code : codeList) {
                clsList.add(of(code).get());
            }
            return clsList;
        }

        /**
         * Get the list of group classification elements. (returns new copied list) <br>
         * the most recommended method <br>
         * The group elements:[ByHand]
         * @return The snapshot list of classification elements in the group. (NotNull)
         */
        public static List<PaymentMethod> listOfRecommended() {
            return new ArrayList<PaymentMethod>(Arrays.asList(ByHand));
        }

        /**
         * Get the list of classification elements in the specified group. (returns new copied list) <br>
         * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
         * @return The snapshot list of classification elements in the group. (NotNull, EmptyAllowed: if the group is not found)
         */
        public static List<PaymentMethod> groupOf(String groupName) {
            if ("recommended".equals(groupName)) {
                return listOfRecommended();
            }
            return new ArrayList<PaymentMethod>(4);
        }

        @Override
        public String toString() {
            return code();
        }
    }

    public enum DefMeta implements ClassificationMeta {
        /** general boolean classification for every flg-column */
        Flg,
        /** status of member from entry to withdrawal */
        MemberStatus,
        /** rank of service member gets */
        ServiceRank,
        /** mainly region of member address */
        Region,
        /** reason for member withdrawal */
        WithdrawalReason,
        /** category of product. self reference */
        ProductCategory,
        /** status for product */
        ProductStatus,
        /** method of payment for purchase */
        PaymentMethod;
        public String classificationName() {
            return name(); // same as definition name
        }

        public OptionalThing<? extends Classification> of(Object code) {
            if (Flg.name().equals(name())) {
                return MockCDef.Flg.of(code);
            }
            if (MemberStatus.name().equals(name())) {
                return MockCDef.MemberStatus.of(code);
            }
            if (ServiceRank.name().equals(name())) {
                return MockCDef.ServiceRank.of(code);
            }
            if (Region.name().equals(name())) {
                return MockCDef.Region.of(code);
            }
            if (WithdrawalReason.name().equals(name())) {
                return MockCDef.WithdrawalReason.of(code);
            }
            if (ProductCategory.name().equals(name())) {
                return MockCDef.ProductCategory.of(code);
            }
            if (ProductStatus.name().equals(name())) {
                return MockCDef.ProductStatus.of(code);
            }
            if (PaymentMethod.name().equals(name())) {
                return MockCDef.PaymentMethod.of(code);
            }
            throw new IllegalStateException("Unknown definition: " + this); // basically unreachable
        }

        public OptionalThing<? extends Classification> byName(String name) {
            if (Flg.name().equals(name())) {
                return MockCDef.Flg.byName(name);
            }
            if (MemberStatus.name().equals(name())) {
                return MockCDef.MemberStatus.byName(name);
            }
            if (ServiceRank.name().equals(name())) {
                return MockCDef.ServiceRank.byName(name);
            }
            if (Region.name().equals(name())) {
                return MockCDef.Region.byName(name);
            }
            if (WithdrawalReason.name().equals(name())) {
                return MockCDef.WithdrawalReason.byName(name);
            }
            if (ProductCategory.name().equals(name())) {
                return MockCDef.ProductCategory.byName(name);
            }
            if (ProductStatus.name().equals(name())) {
                return MockCDef.ProductStatus.byName(name);
            }
            if (PaymentMethod.name().equals(name())) {
                return MockCDef.PaymentMethod.byName(name);
            }
            throw new IllegalStateException("Unknown definition: " + this); // basically unreachable
        }

        public Classification codeOf(Object code) { // null if not found, old style so use classificationOf(code)
            if (Flg.name().equals(name())) {
                return MockCDef.Flg.codeOf(code);
            }
            if (MemberStatus.name().equals(name())) {
                return MockCDef.MemberStatus.codeOf(code);
            }
            if (ServiceRank.name().equals(name())) {
                return MockCDef.ServiceRank.codeOf(code);
            }
            if (Region.name().equals(name())) {
                return MockCDef.Region.codeOf(code);
            }
            if (WithdrawalReason.name().equals(name())) {
                return MockCDef.WithdrawalReason.codeOf(code);
            }
            if (ProductCategory.name().equals(name())) {
                return MockCDef.ProductCategory.codeOf(code);
            }
            if (ProductStatus.name().equals(name())) {
                return MockCDef.ProductStatus.codeOf(code);
            }
            if (PaymentMethod.name().equals(name())) {
                return MockCDef.PaymentMethod.codeOf(code);
            }
            throw new IllegalStateException("Unknown definition: " + this); // basically unreachable
        }

        public Classification nameOf(String name) { // null if not found, old style so use classificationByName(name)
            if (Flg.name().equals(name())) {
                return MockCDef.Flg.valueOf(name);
            }
            if (MemberStatus.name().equals(name())) {
                return MockCDef.MemberStatus.valueOf(name);
            }
            if (ServiceRank.name().equals(name())) {
                return MockCDef.ServiceRank.valueOf(name);
            }
            if (Region.name().equals(name())) {
                return MockCDef.Region.valueOf(name);
            }
            if (WithdrawalReason.name().equals(name())) {
                return MockCDef.WithdrawalReason.valueOf(name);
            }
            if (ProductCategory.name().equals(name())) {
                return MockCDef.ProductCategory.valueOf(name);
            }
            if (ProductStatus.name().equals(name())) {
                return MockCDef.ProductStatus.valueOf(name);
            }
            if (PaymentMethod.name().equals(name())) {
                return MockCDef.PaymentMethod.valueOf(name);
            }
            throw new IllegalStateException("Unknown definition: " + this); // basically unreachable
        }

        public List<Classification> listAll() {
            if (Flg.name().equals(name())) {
                return toClsList(MockCDef.Flg.listAll());
            }
            if (MemberStatus.name().equals(name())) {
                return toClsList(MockCDef.MemberStatus.listAll());
            }
            if (ServiceRank.name().equals(name())) {
                return toClsList(MockCDef.ServiceRank.listAll());
            }
            if (Region.name().equals(name())) {
                return toClsList(MockCDef.Region.listAll());
            }
            if (WithdrawalReason.name().equals(name())) {
                return toClsList(MockCDef.WithdrawalReason.listAll());
            }
            if (ProductCategory.name().equals(name())) {
                return toClsList(MockCDef.ProductCategory.listAll());
            }
            if (ProductStatus.name().equals(name())) {
                return toClsList(MockCDef.ProductStatus.listAll());
            }
            if (PaymentMethod.name().equals(name())) {
                return toClsList(MockCDef.PaymentMethod.listAll());
            }
            throw new IllegalStateException("Unknown definition: " + this); // basically unreachable
        }

        public List<Classification> listByGroup(String groupName) { // exception if not found
            if (Flg.name().equals(name())) {
                return toClsList(MockCDef.Flg.listByGroup(groupName));
            }
            if (MemberStatus.name().equals(name())) {
                return toClsList(MockCDef.MemberStatus.listByGroup(groupName));
            }
            if (ServiceRank.name().equals(name())) {
                return toClsList(MockCDef.ServiceRank.listByGroup(groupName));
            }
            if (Region.name().equals(name())) {
                return toClsList(MockCDef.Region.listByGroup(groupName));
            }
            if (WithdrawalReason.name().equals(name())) {
                return toClsList(MockCDef.WithdrawalReason.listByGroup(groupName));
            }
            if (ProductCategory.name().equals(name())) {
                return toClsList(MockCDef.ProductCategory.listByGroup(groupName));
            }
            if (ProductStatus.name().equals(name())) {
                return toClsList(MockCDef.ProductStatus.listByGroup(groupName));
            }
            if (PaymentMethod.name().equals(name())) {
                return toClsList(MockCDef.PaymentMethod.listByGroup(groupName));
            }
            throw new IllegalStateException("Unknown definition: " + this); // basically unreachable
        }

        public List<Classification> listOf(Collection<String> codeList) {
            if (Flg.name().equals(name())) {
                return toClsList(MockCDef.Flg.listOf(codeList));
            }
            if (MemberStatus.name().equals(name())) {
                return toClsList(MockCDef.MemberStatus.listOf(codeList));
            }
            if (ServiceRank.name().equals(name())) {
                return toClsList(MockCDef.ServiceRank.listOf(codeList));
            }
            if (Region.name().equals(name())) {
                return toClsList(MockCDef.Region.listOf(codeList));
            }
            if (WithdrawalReason.name().equals(name())) {
                return toClsList(MockCDef.WithdrawalReason.listOf(codeList));
            }
            if (ProductCategory.name().equals(name())) {
                return toClsList(MockCDef.ProductCategory.listOf(codeList));
            }
            if (ProductStatus.name().equals(name())) {
                return toClsList(MockCDef.ProductStatus.listOf(codeList));
            }
            if (PaymentMethod.name().equals(name())) {
                return toClsList(MockCDef.PaymentMethod.listOf(codeList));
            }
            throw new IllegalStateException("Unknown definition: " + this); // basically unreachable
        }

        public List<Classification> groupOf(String groupName) { // old style
            if (Flg.name().equals(name())) {
                return toClsList(MockCDef.Flg.groupOf(groupName));
            }
            if (MemberStatus.name().equals(name())) {
                return toClsList(MockCDef.MemberStatus.groupOf(groupName));
            }
            if (ServiceRank.name().equals(name())) {
                return toClsList(MockCDef.ServiceRank.groupOf(groupName));
            }
            if (Region.name().equals(name())) {
                return toClsList(MockCDef.Region.groupOf(groupName));
            }
            if (WithdrawalReason.name().equals(name())) {
                return toClsList(MockCDef.WithdrawalReason.groupOf(groupName));
            }
            if (ProductCategory.name().equals(name())) {
                return toClsList(MockCDef.ProductCategory.groupOf(groupName));
            }
            if (ProductStatus.name().equals(name())) {
                return toClsList(MockCDef.ProductStatus.groupOf(groupName));
            }
            if (PaymentMethod.name().equals(name())) {
                return toClsList(MockCDef.PaymentMethod.groupOf(groupName));
            }
            throw new IllegalStateException("Unknown definition: " + this); // basically unreachable
        }

        @SuppressWarnings("unchecked")
        private List<Classification> toClsList(List<?> clsList) {
            return (List<Classification>) clsList;
        }

        public ClassificationCodeType codeType() {
            if (Flg.name().equals(name())) {
                return ClassificationCodeType.Number;
            }
            if (MemberStatus.name().equals(name())) {
                return ClassificationCodeType.String;
            }
            if (ServiceRank.name().equals(name())) {
                return ClassificationCodeType.String;
            }
            if (Region.name().equals(name())) {
                return ClassificationCodeType.Number;
            }
            if (WithdrawalReason.name().equals(name())) {
                return ClassificationCodeType.String;
            }
            if (ProductCategory.name().equals(name())) {
                return ClassificationCodeType.String;
            }
            if (ProductStatus.name().equals(name())) {
                return ClassificationCodeType.String;
            }
            if (PaymentMethod.name().equals(name())) {
                return ClassificationCodeType.String;
            }
            return ClassificationCodeType.String; // as default
        }

        public ClassificationUndefinedHandlingType undefinedHandlingType() {
            if (Flg.name().equals(name())) {
                return ClassificationUndefinedHandlingType.EXCEPTION;
            }
            if (MemberStatus.name().equals(name())) {
                return ClassificationUndefinedHandlingType.EXCEPTION;
            }
            if (ServiceRank.name().equals(name())) {
                return ClassificationUndefinedHandlingType.EXCEPTION;
            }
            if (Region.name().equals(name())) {
                return ClassificationUndefinedHandlingType.EXCEPTION;
            }
            if (WithdrawalReason.name().equals(name())) {
                return ClassificationUndefinedHandlingType.EXCEPTION;
            }
            if (ProductCategory.name().equals(name())) {
                return ClassificationUndefinedHandlingType.EXCEPTION;
            }
            if (ProductStatus.name().equals(name())) {
                return ClassificationUndefinedHandlingType.EXCEPTION;
            }
            if (PaymentMethod.name().equals(name())) {
                return ClassificationUndefinedHandlingType.EXCEPTION;
            }
            return ClassificationUndefinedHandlingType.LOGGING; // as default
        }

        public static OptionalThing<MockCDef.DefMeta> find(String classificationName) { // instead of valueOf()
            if (classificationName == null) {
                throw new IllegalArgumentException("The argument 'classificationName' should not be null.");
            }
            if (Flg.name().equalsIgnoreCase(classificationName)) {
                return OptionalThing.of(MockCDef.DefMeta.Flg);
            }
            if (MemberStatus.name().equalsIgnoreCase(classificationName)) {
                return OptionalThing.of(MockCDef.DefMeta.MemberStatus);
            }
            if (ServiceRank.name().equalsIgnoreCase(classificationName)) {
                return OptionalThing.of(MockCDef.DefMeta.ServiceRank);
            }
            if (Region.name().equalsIgnoreCase(classificationName)) {
                return OptionalThing.of(MockCDef.DefMeta.Region);
            }
            if (WithdrawalReason.name().equalsIgnoreCase(classificationName)) {
                return OptionalThing.of(MockCDef.DefMeta.WithdrawalReason);
            }
            if (ProductCategory.name().equalsIgnoreCase(classificationName)) {
                return OptionalThing.of(MockCDef.DefMeta.ProductCategory);
            }
            if (ProductStatus.name().equalsIgnoreCase(classificationName)) {
                return OptionalThing.of(MockCDef.DefMeta.ProductStatus);
            }
            if (PaymentMethod.name().equalsIgnoreCase(classificationName)) {
                return OptionalThing.of(MockCDef.DefMeta.PaymentMethod);
            }
            return OptionalThing.ofNullable(null, () -> {
                throw new ClassificationNotFoundException("Unknown classification: " + classificationName);
            });
        }

        public static MockCDef.DefMeta meta(String classificationName) { // old style so use find(name)
            if (classificationName == null) {
                throw new IllegalArgumentException("The argument 'classificationName' should not be null.");
            }
            if (Flg.name().equalsIgnoreCase(classificationName)) {
                return MockCDef.DefMeta.Flg;
            }
            if (MemberStatus.name().equalsIgnoreCase(classificationName)) {
                return MockCDef.DefMeta.MemberStatus;
            }
            if (ServiceRank.name().equalsIgnoreCase(classificationName)) {
                return MockCDef.DefMeta.ServiceRank;
            }
            if (Region.name().equalsIgnoreCase(classificationName)) {
                return MockCDef.DefMeta.Region;
            }
            if (WithdrawalReason.name().equalsIgnoreCase(classificationName)) {
                return MockCDef.DefMeta.WithdrawalReason;
            }
            if (ProductCategory.name().equalsIgnoreCase(classificationName)) {
                return MockCDef.DefMeta.ProductCategory;
            }
            if (ProductStatus.name().equalsIgnoreCase(classificationName)) {
                return MockCDef.DefMeta.ProductStatus;
            }
            if (PaymentMethod.name().equalsIgnoreCase(classificationName)) {
                return MockCDef.DefMeta.PaymentMethod;
            }
            throw new IllegalStateException("Unknown classification: " + classificationName);
        }

        @SuppressWarnings("unused")
        private String[] xinternalEmptyString() {
            return EMPTY_STRINGS; // to suppress 'unused' warning of import statement
        }
    }
}
