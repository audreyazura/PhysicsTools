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
package albanlafuente.physicstools.math;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 * Represents a continuous function as an ensemble of value associated with an abscissa
 * To make up for the fact there is a finite number of abscissa, value in-between are approximated by doing a linear interpolation between the two closest points
 * @author Alban Lafuente
 */
public class ContinuousFunction
{
    protected final Map<BigDecimal, BigDecimal> m_values;
    protected final TreeSet<BigDecimal> m_abscissa;
    
    public ContinuousFunction()
    {
        m_values = new HashMap<>();
        m_abscissa = new TreeSet<>();
    }
    
    public ContinuousFunction (ContinuousFunction p_passedFunction)
    {
        HashMap<BigDecimal, BigDecimal> valuesToCopy = p_passedFunction.getFunction();
        m_values = new HashMap<>();
        for (BigDecimal abscissa: valuesToCopy.keySet())
        {
            m_values.put(abscissa, valuesToCopy.get(abscissa));
        }
        
        m_abscissa = new TreeSet<BigDecimal>(m_values.keySet());
    }
    
    public ContinuousFunction (HashMap<BigDecimal, BigDecimal> p_values)
    {
        m_values = new HashMap<>();
        for (BigDecimal abscissa: p_values.keySet())
        {
            m_values.put(abscissa, p_values.get(abscissa));
        }
        
        m_abscissa = new TreeSet<BigDecimal>(m_values.keySet());
    }
    
    /**
     * Create a {@code ContinuousFunction} from a file given by SCAPS-1D file
     * @param p_inputFile the SCAPS file from which the values of the abscissa and the field need to be extracted
     * @param p_abscissaUnitMultiplier the multiplier to convert the abscissa to metres
     * @param p_valuesUnitMultiplier the multiplier to convert the field values to SI
     * @param p_expectedExtension the extension of the file from which the value need to be extracted
     * @param p_separator the character string separating the two column in the file
     * @param p_ncolumn  the total number of column in the type of file given
     * @param p_columnToExtract the column to be taken, first number is the abscissa, second the value of the continuous function
     * @throws FileNotFoundException
     * @throws DataFormatException
     * @throws ArrayIndexOutOfBoundsException
     * @throws IOException 
     */
    public ContinuousFunction (File p_inputFile, BigDecimal p_abscissaUnitMultiplier, BigDecimal p_valuesUnitMultiplier, String p_expectedExtension, String p_separator, int p_ncolumn, int[] p_columnToExtract) throws FileNotFoundException, DataFormatException, ArrayIndexOutOfBoundsException, IOException
    {
        m_values = new HashMap<>();
        m_abscissa = new TreeSet<>();
        
        String[] nameSplit = p_inputFile.getPath().split("\\.");
        
        if (!nameSplit[nameSplit.length-1].equals(p_expectedExtension))
        {
            throw new DataFormatException();
        }
        
        BufferedReader fieldFile = new BufferedReader(new FileReader(p_inputFile));
        Pattern numberRegex = Pattern.compile("^\\-?\\d+(\\.\\d+(e(\\+|\\-)\\d+)?)?");
	
	String line;
	while (((line = fieldFile.readLine()) != null))
	{	    
	    String[] lineSplit = line.strip().split(p_separator);
	    
	    if(lineSplit.length == p_ncolumn && numberRegex.matcher(lineSplit[0]).matches())
	    {
		//we put the abscissa in meter in order to do all calculations in SI
                BigDecimal currentAbscissa = formatBigDecimal((new BigDecimal(lineSplit[p_columnToExtract[0]].strip())).multiply(p_abscissaUnitMultiplier));
                
                if (!m_abscissa.contains(currentAbscissa))
                {
                    m_values.put(currentAbscissa, formatBigDecimal((new BigDecimal(lineSplit[p_columnToExtract[1]].strip())).multiply(p_valuesUnitMultiplier)));
                    m_abscissa.add(currentAbscissa);
                }
	    }
        }
    }
    
    private BigDecimal integrateTwoClosePoints(BigDecimal p_lowerBound, BigDecimal p_upperBound) throws IllegalArgumentException
    {
        if (p_lowerBound.compareTo(p_upperBound) > 0)
        {
            throw new IllegalArgumentException("Lower bound higher than upper bound.");
        }
        
        /**
         * In these class, the function are approximated to line segment between two points. Therefore, the integral can be calculated as the area of a trapezoid between two known points.
         *    /|
         *   / |
         *  /  |
         * /_T_|
         * |   |
         * | R |
         * |   |
         * |___|
         * 
         * Integral = Area (T) + Area (R)
         */
        BigDecimal basis = p_upperBound.subtract(p_lowerBound);
        BigDecimal lowerBoundValue = getValueAtPosition(p_lowerBound);
        BigDecimal upperBoundValue = getValueAtPosition(p_upperBound);
        BigDecimal rectangleHeight = lowerBoundValue.min(upperBoundValue);
        
        BigDecimal rectangleArea = basis.multiply(rectangleHeight);
        BigDecimal triangleArea = basis.multiply((lowerBoundValue.max(upperBoundValue)).subtract(rectangleHeight)).divide(new BigDecimal("2"), MathContext.DECIMAL128);
        
        return rectangleArea.add(triangleArea);
    }
    
    /**
     * Tell if the passed position is comprised between the minimum and maximum abscissa of the continuous function
     * @param p_position
     * @return true if the position is between the two extrema
     */
    public boolean isInRange(BigDecimal p_position)
    {
        return p_position.compareTo(m_abscissa.first()) >= 0 && p_position.compareTo(m_abscissa.last()) <= 0;
    }
    
    /**
     * The default {@code BigDecimal} formatting function for the ContinuousFunction. Called at the creation of a Function so number are consistent between them and they can be compared more easily.
     * @param p_toBeFormatted
     * @return the formatted BigDecimal
     */
    protected BigDecimal formatBigDecimal(BigDecimal p_toBeFormatted)
    {
        return p_toBeFormatted.stripTrailingZeros();
    }
    
    /**
     * add two {@code ContinuousFunction} together
     * @param p_passedFunction the function to be added.
     * @return {@code this} + {@code p_passedFunction}
     */
    public ContinuousFunction add(ContinuousFunction p_passedFunction)
    {
        Map<BigDecimal, BigDecimal> addedValues = new HashMap<>();
        
        for (BigDecimal position: m_abscissa)
        {
            try
            {
                addedValues.put(position, formatBigDecimal(m_values.get(position).add(p_passedFunction.getValueAtPosition(position))));
            }
            catch (NoSuchElementException ex)
            {
                addedValues.put(position, m_values.get(position));
            }
        }
        
        return new ContinuousFunction((HashMap<BigDecimal, BigDecimal>) addedValues);
    }
    
    /**
     * remove zeros from the {@code ContinuousFunction}, replacing them with {@code DOUBLE_MIN_VALUE} with a sign depending of the function environment.
     * @return a new {@code ContinuousFunction} without zeros
     */
    public ContinuousFunction avoidZeros()
    {
        Map<BigDecimal, BigDecimal> noZeroFunction = new HashMap<BigDecimal, BigDecimal>();
        List<BigDecimal> abscissa = new ArrayList<BigDecimal>(m_values.keySet());
        BigDecimal next, previous, currentValue, currentAbscissa;                
        int lastIndex = abscissa.size()-1;
        
        /*
        if there is only one point in the function (why would you do that?)
        we test if the only element is zero and put in a positive DOUBLE_MIN_VALUE in if it is
        */
        if (lastIndex == 0)
        {
            currentAbscissa = abscissa.get(0);
            currentValue = m_values.get(currentAbscissa);
            
            if (currentValue.compareTo(BigDecimal.ZERO) == 0)
            {
                noZeroFunction.put(currentAbscissa, new BigDecimal(Double.MIN_VALUE));
            }
            else
            {
                noZeroFunction.put(currentAbscissa, currentValue);
            }
        }
        /*
        Otherwise we loop on all element, testing each of them to be zero
        If they are, we put instead DOUBLE_MIN_VALUE in, with a sign given by the environment
        */
        else
        {
            for(int i = 0 ; i <= lastIndex ; i += 1)
            {
                currentAbscissa = abscissa.get(i);
                currentValue = m_values.get(currentAbscissa);

                if (currentValue.compareTo(BigDecimal.ZERO) == 0)
                {
                    if (i == lastIndex)
                    {
                        noZeroFunction.put(currentAbscissa, new BigDecimal(m_values.get(abscissa.get(i-1)).signum() * Double.MIN_VALUE));
                    }
                    else
                    {
                        next = m_values.get(abscissa.get(i+1));
                        
                        if (i == 0)
                        {
                            noZeroFunction.put(currentAbscissa, new BigDecimal(next.signum() * Double.MIN_VALUE));
                        }
                        else
                        {
                            previous = noZeroFunction.get(abscissa.get(i-1));
                            
                            if (currentValue.subtract(next).abs().compareTo(currentValue.subtract(previous).abs()) > 0)
                            {
                                noZeroFunction.put(currentAbscissa, new BigDecimal(previous.signum() * Double.MIN_VALUE));
                            }
                            else 
                            {
                                noZeroFunction.put(currentAbscissa, new BigDecimal(next.signum() * Double.MIN_VALUE));
                            }
                        }
                    }
                }
                else
                {
                    noZeroFunction.put(currentAbscissa, currentValue);
                }
            }
        }
        
        return new ContinuousFunction((HashMap<BigDecimal, BigDecimal>) noZeroFunction);
    }
    
    /**
     * divide {@code this} by the passed {@code ContinuousFunction} by dividing each of its values by the value at the corresponding abscissa in the passed {@code ContinuousFunction}.
     * @param p_passedFunction the dividing {@code ContinuousFunction}
     * @return {@code this} / {@code p_passedFunction}
     * @throws ArithmeticException 
     */
    public ContinuousFunction divide(ContinuousFunction p_passedFunction) throws ArithmeticException
    {
        return this.multiply(p_passedFunction.invert());
    }
    
    /**
     * divide a {@code ContinuousFunction} by the passed {@code BigDecimal}, by dividing each of its value by it
     * @param p_divider the {@code BigDecimal} by which dividing the {@code ContinuousFunction}
     * @return {@code this} / {@code p_divider}
     * @throws ArithmeticException 
     */
    public ContinuousFunction divide(BigDecimal p_divider) throws ArithmeticException
    {
        return this.multiply(BigDecimal.ONE.divide(p_divider, MathContext.DECIMAL128));
    }
    
    public BigDecimal end()
    {
        return m_abscissa.last();
    }
    
    @Override
    public boolean equals(Object o)
    {
        boolean result = this.getClass().equals(o.getClass());
        
        if (result)
        {
            TreeSet<BigDecimal> passedAbscissa = ((ContinuousFunction) o).getAbscissa();
            result &= m_values.size() == passedAbscissa.size();
            
            if (result)
            {
                Iterator<BigDecimal> abscissaIterator = m_abscissa.iterator();
                BigDecimal key;
                while (result && abscissaIterator.hasNext())
                {
                    key = abscissaIterator.next();
                    result &= passedAbscissa.contains(key);
                    result &= m_values.get(key).compareTo(((ContinuousFunction) o).getValueAtPosition(key)) == 0;
                }
            }
        }
        
        return result;
    }
    
    /**
     * return a copy of the {@code ContinuousFunction} abscissa as a {@code TreeSet}
     * @return {@code new TreeSet(abscissa)}
     */
    public TreeSet<BigDecimal> getAbscissa()
    {
        return new TreeSet<BigDecimal>(m_abscissa);
    }
    
    public BigDecimal getMeanIntervalSize()
    {
        BigDecimal functionSpan = (m_abscissa.last().subtract(m_abscissa.first())).abs();
        BigDecimal numberOfInterval = new BigDecimal(m_abscissa.size());
        
        return functionSpan.divide(numberOfInterval, MathContext.DECIMAL128);
    }
    
    /**
     * return a copy of the {@code HashMap} linking the abscissa to the values of the function
     * @return {@code new HashMap(values)}
     */
    public HashMap<BigDecimal, BigDecimal> getFunction()
    {
        HashMap<BigDecimal, BigDecimal> returnMap = new HashMap<>();
        
        for (BigDecimal key: m_abscissa)
        {
            returnMap.put(new BigDecimal(key.toString()), new BigDecimal(m_values.get(key).toString()));
        }
        
        return returnMap;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(m_values);
    }
    
    /**
     * Integrate the function on its whole range
     * @return The integration of the function
     * @throws IndexOutOfBoundsException
     * @throws IllegalArgumentException 
     */
    public BigDecimal integrate() throws IndexOutOfBoundsException, IllegalArgumentException
    {
        return integrate(m_abscissa.first(), m_abscissa.last());
    }
    
    /**
     * Integrate the function on a given range
     * @param p_lowerBound The lower abscissa of the integration
     * @param p_upperBound The upper abscissa of the integration
     * @return The value of the integral function on the given range
     * @throws IndexOutOfBoundsException
     * @throws IllegalArgumentException 
     */
    public BigDecimal integrate(BigDecimal p_lowerBound, BigDecimal p_upperBound) throws IndexOutOfBoundsException, IllegalArgumentException
    {
        if (p_lowerBound.compareTo(m_abscissa.last()) >= 0 || p_upperBound.compareTo(m_abscissa.first()) <= 0 || p_lowerBound.compareTo(m_abscissa.first()) < 0 || p_upperBound.compareTo(m_abscissa.last()) > 0)
        {
            throw new IndexOutOfBoundsException("Bounds outside the function range: function definition (" + m_abscissa.first() + ", " + m_abscissa.last() + "), bounds given: (" + p_lowerBound + ", " + p_upperBound + ")");
        }
        
        if (p_lowerBound.compareTo(p_upperBound) > 0)
        {
            throw new IllegalArgumentException("Lower bound higher than upperbound.");
        }
        
        BigDecimal integration = BigDecimal.ZERO;

        //Finding what the closest defined abscissa that is higher than lower bound
        Iterator<BigDecimal> abscissaIterator = m_abscissa.iterator();
        BigDecimal prevAbscissa = p_lowerBound;
        BigDecimal nextAbscissa = p_lowerBound;
        while (abscissaIterator.hasNext() && (nextAbscissa = abscissaIterator.next()).compareTo(p_lowerBound) <= 0)
        {
        }
        
        //if the next closest abscissa is higher that the upper bound, we redefine nextAbscissa as the upper bound
        if (nextAbscissa.compareTo(p_upperBound) <= 0)
        {
            //integrating
            do
            {
                integration = integration.add(integrateTwoClosePoints(prevAbscissa, nextAbscissa));
                prevAbscissa = nextAbscissa;
            }while (abscissaIterator.hasNext() && (nextAbscissa = abscissaIterator.next()).compareTo(p_upperBound) <= 0);
        }
        
        //the last segment, between the previous nextAbscissa et the upper bound has not yet been done, so we do it
        integration = integration.add(integrateTwoClosePoints(prevAbscissa, p_upperBound));
        
        return integration;
    }
    
    /**
     * return a {@code ContinuousFunction} which values are the invert of {@code this} values
     * @return 1/{@code this}
     * @throws ArithmeticException 
     */
    public ContinuousFunction invert() throws ArithmeticException
    {
        Map<BigDecimal, BigDecimal> invertedFunction = new HashMap<>();
        
        for (BigDecimal position: m_abscissa)
        {
            invertedFunction.put(position, BigDecimal.ONE.divide(getValueAtPosition(position), MathContext.DECIMAL128));
        }
        
        return new ContinuousFunction((HashMap<BigDecimal, BigDecimal>) invertedFunction);
    }
    
    public HashMap<String, BigDecimal> maximum()
    {
        if (m_abscissa.isEmpty())
        {
            throw new IllegalStateException("Function was not initialized.");
        }
        
        Iterator<BigDecimal> abscissaIterator = m_abscissa.iterator();
        BigDecimal currentAbscissa = abscissaIterator.next();
        BigDecimal maxAbscissa = currentAbscissa;
        BigDecimal maxValue = m_values.get(maxAbscissa);
        
        while (abscissaIterator.hasNext())
        {
            currentAbscissa = abscissaIterator.next();
            
            if (m_values.get(currentAbscissa).compareTo(maxValue) > 0)
            {
                maxAbscissa = currentAbscissa;
                maxValue = m_values.get(currentAbscissa);
            }
        }
        
        HashMap<String, BigDecimal> result = new HashMap<>();
        result.put("abscissa", maxAbscissa);
        result.put("value", maxValue);
        
        return result;
    }
    
    /**
     * multiply two {@code ContinuousFunction} together
     * @param p_passedFunction the function to multiply with the current one
     * @return {@code this} * {@code p_passedFunction}
     */
    public ContinuousFunction multiply(ContinuousFunction p_passedFunction)
    {
        Map<BigDecimal, BigDecimal> multilpliedValues = new HashMap<>();
        
        for (BigDecimal position: m_abscissa)
        {
            try
            {
                multilpliedValues.put(position, formatBigDecimal(m_values.get(position).multiply(p_passedFunction.getValueAtPosition(position))));
            }
            catch (NoSuchElementException ex)
            {
                multilpliedValues.put(position, m_values.get(position));
            }
        }
        
        return new ContinuousFunction((HashMap<BigDecimal, BigDecimal>) multilpliedValues);
    }
    
    /**
     * multiply a {@code ContinuousFunction} with a {@code BigDecimal} by multiplying each values of the function by the passed {@code BigDecimal}
     * @param p_multiplier the {@code BigDecimal} by which the function is to be multiplied
     * @return {@code this} * {@code p_multiplier}
     */
    public ContinuousFunction multiply(BigDecimal p_multiplier)
    {
        Map<BigDecimal, BigDecimal> multilpliedValues = new HashMap<>();
        
        for (BigDecimal position: m_abscissa)
        {
            multilpliedValues.put(position, formatBigDecimal(getValueAtPosition(position).multiply(p_multiplier)));
        }
        
        return new ContinuousFunction((HashMap<BigDecimal, BigDecimal>) multilpliedValues);
    }
    
    /**
     * return a new {@code ContinuousFunction} which associate at each abscissa -1 * the value of the Function at this abscissa
     * @return {@code -this}
     */
    public ContinuousFunction negate()
    {
        Map<BigDecimal, BigDecimal> negatedFunction = new HashMap<>();
        
        for (BigDecimal position: m_abscissa)
        {
            negatedFunction.put(position, m_values.get(position).negate());
        }
        
        return new ContinuousFunction((HashMap<BigDecimal, BigDecimal>) negatedFunction);
    }
    
    public BigDecimal start()
    {
        return m_abscissa.first();
    }
    
    /**
     * subtract two {@code ContinuousFunction}
     * @param p_passedFunction
     * @return {@code this} - {@code p_passedFunction}
     */
    public ContinuousFunction subtract(ContinuousFunction p_passedFunction)
    {
        return this.add(p_passedFunction.negate());
    }
    
    @Override
    public String toString()
    {
        String result = "";
        
        for (BigDecimal currentAbscissa: m_abscissa)
        {
            result = result.concat(currentAbscissa+"\t=> "+m_values.get(currentAbscissa)+"\n");
        }
        
        return result;
    }
            
    /**
     * Give the value of the continuous function at the given position
     * If the position given is not in the abscissa list of the continuous function, the value is approximating by doing a linear approximation between the two closes points
     * @param position the abscissa at which to find the value
     * @return the value of the {@code ContinuousFunction} at this position, or its linear approximation
     */
    synchronized public BigDecimal getValueAtPosition(BigDecimal position)
    {
        BigDecimal value;
        BigDecimal cleanPosition = position.stripTrailingZeros();
        
        if (isInRange(cleanPosition))
        {
            if (m_abscissa.contains(cleanPosition))
            {
                value = m_values.get(cleanPosition);
            }
            else
            {
                BigDecimal previousPosition = m_abscissa.lower(cleanPosition);
                BigDecimal nextPosition = m_abscissa.higher(cleanPosition);
                
                BigDecimal interpolationSlope = (m_values.get(nextPosition).subtract(m_values.get(previousPosition))).divide(nextPosition.subtract(previousPosition), MathContext.DECIMAL128);
                BigDecimal interpolationOffset = m_values.get(previousPosition).subtract(interpolationSlope.multiply(previousPosition));
                
                value = (interpolationSlope.multiply(cleanPosition)).add(interpolationOffset);
            }
        }
        else
        {
            throw new IndexOutOfBoundsException("No field value for position:" + String.valueOf(cleanPosition));
        }

        return new BigDecimal(value.toString());
    }
}

