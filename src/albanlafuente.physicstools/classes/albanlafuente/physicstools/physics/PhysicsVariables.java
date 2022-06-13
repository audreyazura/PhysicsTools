/*
 * Copyright (C) 2021 Alban Lafuente
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package albanlafuente.physicstools.physics;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 *
 * @author audreyazura
 */
public class PhysicsVariables
{
    //pi
    static final public BigDecimal PI = new BigDecimal("3.1415926535");
    //Boltzman constant in J/K
    static final public BigDecimal KB = new BigDecimal("1.380649e-23");
    //electron mass in kg
    static final public BigDecimal ME = new BigDecimal("9.10938188e-31");
    //elementary charge in C
    static final public BigDecimal Q = new BigDecimal("1.60217733e-19");
    //definition of the electronVolt in J
    static final public BigDecimal EV = new BigDecimal("1.602176634e-19");
    //speed of light in m/s
    static final public BigDecimal c = new BigDecimal("299792458");
    //Planck constant in J.s
    static final public BigDecimal h = new BigDecimal("6.62607015e-34");
    //Reduced Planck constant in J.s
    static final public BigDecimal hbar = h.divide(PI.multiply(new BigDecimal("2")), MathContext.DECIMAL128);
    
    /**
     * lists the different unit prefixes and their multiplier in SI
     * the UNITY prefix is there to represent "pure" SI units, such as m, s, g...
     */
    static public enum UnitsPrefix
    {
        FEMTO ("1e-15", "f"),   //[SI unit]/[FEMTO unit]
        PICO ("1e-12", "p"),    //[SI unit]/[PICO unit]
        NANO ("1e-9", "n"),     //[SI unit]/[NANO unit]
        MICRO ("1e-6", "μ"),    //[SI unit]/[MICRO unit]
        MILLI ("1e-3", "m"),    //[SI unit]/[MILLI unit]
        CENTI ("1e-2", "c"),    //[SI unit]/[CENTI unit]
        UNITY ("1.0", "");      //[SI unit]/[SI unit]
        
        private final BigDecimal m_multiplier;
        private final String m_textPrefix;

        private UnitsPrefix(String p_multiplier, String p_prefix)
        {
            m_multiplier = new BigDecimal(p_multiplier);
            m_textPrefix = new String(p_prefix);
        }
        
        public BigDecimal getMultiplier()
        {
            return m_multiplier;
        }
        
        public String getPrefix()
        {
            return m_textPrefix;
        }
        
        public int getScale()
        {
            return m_multiplier.scale();
        }
        
        /**
         * Select the correct unit prefix for a given passed unit
         * @param p_unit the passed unit (e.g., nm, fs...)
         * @return the correct unit prefix
         */
        static public UnitsPrefix selectPrefix (String p_unit)
        {
            UnitsPrefix prefixSelected;

            switch (p_unit.charAt(0))
            {
                case 'f':
                    prefixSelected = FEMTO;
                    break;
                case 'p':
                    prefixSelected = PICO;
                    break;
                case 'n':
                    prefixSelected = NANO;
                    break;
                case 'μ':
                    prefixSelected = MICRO;
                    break;
                case 'm':
                    prefixSelected = MILLI;
                    break;
                case 'c':
                    prefixSelected = CENTI;
                    break;
                default:
                    prefixSelected = UNITY;
                    break;
            }

            return prefixSelected;
        }
    }
}
