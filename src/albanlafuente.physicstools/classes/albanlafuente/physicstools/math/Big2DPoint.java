/*
 * Copyright (C) 2022 audreyazura
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

import albanlafuente.physicstools.physics.PhysicsVariables;
import java.math.BigDecimal;
import java.math.MathContext;
import org.nevec.rjm.BigDecimalMath;

/**
 *
 * @author audreyazura
 */
public class Big2DPoint
{
    private final BigDecimal m_x;
    private final BigDecimal m_y;
    
    public Big2DPoint ()
    {
        m_x = BigDecimal.ZERO;
        m_y = BigDecimal.ZERO;
    }
    
    public Big2DPoint (BigDecimal p_x, BigDecimal p_y)
    {
        m_x = new BigDecimal(p_x.toString());
        m_y = new BigDecimal(p_y.toString());
    }
    
    public Big2DPoint (Big2DPoint p_point)
    {
        m_x = p_point.getX();
        m_y = p_point.getY();
    }
    
    public BigDecimal distanceTo(Big2DPoint p_point)
    {
        BigDecimal xDistance = m_x.subtract(p_point.getX());
        BigDecimal yDistance = m_y.subtract(p_point.getY());
        
        BigDecimal result;
        if (xDistance.compareTo(BigDecimal.ZERO) == 0 && yDistance.compareTo(BigDecimal.ZERO) == 0)
        {
            result = BigDecimal.ZERO;
        }
        else
        {
            result = BigDecimalMath.sqrt((xDistance.pow(2)).add(yDistance.pow(2)), MathContext.DECIMAL128);
        }
        
        return result;
    }
    
    public BigDecimal getX()
    {
        return new BigDecimal(m_x.toString());
    }
    
    public BigDecimal getY()
    {
        return new BigDecimal(m_y.toString());
    }
    
    public String toScaledString(PhysicsVariables.UnitsPrefix unit)
    {
        return "(" + m_x.divide(unit.getMultiplier(), MathContext.DECIMAL128).stripTrailingZeros().toString() + "; " + m_y.divide(unit.getMultiplier(), MathContext.DECIMAL128).stripTrailingZeros().toString() + ")";
    }
    
    @Override
    public String toString()
    {
        return "(" + m_x.toString() + "; " + m_y.toString() + ")";
    }
    
    public Big2DPoint translate(Big2DVector translationVector)
    {
        return new Big2DPoint(m_x.add(translationVector.getX()), m_y.add(translationVector.getY()));
    }
}
