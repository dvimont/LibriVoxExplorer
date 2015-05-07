/*
 * Copyright (C) 2015 Daniel Vimont
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

package org.commonvox.le_catalog;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Daniel Vimont
 */
@XmlType(propOrder = { "id", "firstName", "lastName", "yearOfBirth", "yearOfDeath" })
public abstract class Contributor 
        implements HasLibrivoxId, Mergeable, Serializable {
    protected String librivoxId;
    protected String firstName;
    protected String lastName;
    protected String yearOfBirth;
    protected String yearOfDeath;

    @XmlElement(name = "id")
    @Override
    public String getId () {
        return librivoxId;
    }
    
    @Override
    public void setId (String id) {
        this.librivoxId = id;
    }

    @XmlElement(name = "first_name")
    public String getFirstName () {
        if (firstName == null) {
            return null;
        }
        return firstName.trim();
        //return firstName;
    }
    
    public void setFirstName (String inputString) {
        firstName = inputString;
    }

    @XmlElement(name = "last_name")
    public String getLastName () {
        //return lastName.trim();
        return lastName;
    }
    
    public void setLastName (String inputString) {
        lastName = inputString;
    }

    @XmlElement(name = "dob")
    public String getYearOfBirth () {
        return yearOfBirth;
    }
    
    public void setYearOfBirth (String inputString) {
        yearOfBirth = inputString;
    }

    @XmlElement(name = "dod")
    public String getYearOfDeath () {
        return yearOfDeath;
    }
    
    public void setYearOfDeath (String inputString) {
        yearOfDeath = inputString;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append(this.getLastName());
        if (this.getFirstName() != null && !this.getFirstName().isEmpty()) {
            output.append(", ").append(this.getFirstName());
        }
        String birthYear;
        String deathYear;
        if (this.getYearOfBirth() == null || this.getYearOfBirth().isEmpty()) {
            birthYear = "?";
        } else {
            birthYear = this.getYearOfBirth();
        }
        if (this.getYearOfDeath() == null || this.getYearOfDeath().isEmpty()) {
            deathYear = "?";
        } else {
            deathYear = this.getYearOfDeath();
        }
        if (!(birthYear.equals("?") && deathYear.equals("?"))) {
            output.append(" (").append(birthYear)
                    .append("-").append(deathYear).append(")");
        }
        return output.toString();
    }
}
