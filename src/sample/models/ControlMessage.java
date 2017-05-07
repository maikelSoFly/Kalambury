package sample.models;

//
//     Project name: Kalambury
//
//     Created by maikel on 06.05.2017.
//     Copyright © 2017 Mikołaj Stępniewski. All rights reserved.
//

// MESSAGE
// 1 - Clear
// 2 -
// 3 -
// 4 -

import java.io.Serializable;

public class ControlMessage implements Serializable {
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
