package sample.models;

//
//     Project name: Kalambury
//
//     Created by maikel on 06.05.2017.
//     Copyright © 2017 Mikołaj Stępniewski. All rights reserved.
//

import java.io.Serializable;

public class ControlMessage implements Serializable {
    // MESSAGE
    // 0 - Default
    // 1 - Clear
    // 2 - Draw Permit
    private int message;

    public ControlMessage(int message) {
        this.message = message;
    }


    //GETTERS & SETTERS
    public int getMessage() {
        return message;
    }
    public void setMessage(int message) {
        this.message = message;
    }
}
