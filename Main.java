/**
 @author     Poatz
 @version    1.1

 Cat Adventure
 This program must be run from windows command prompt with graphics.txt in the program folder to work properly.
 To import characters, the character files must be in the program folder.

 The program is a simple adventure game with 3 levels. Players can create their own characters to play the game
 with, or use pre-made ones by importing a character file. Upon completing the game, the player's performance is
 written to results.txt. If the player wins, their character is written to (name).txt.

 v1.1 - Fixed an issue where the game wouldn't save results if player lost
 */

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        //main game loop start
        String gameLoopPrompt = "";
        while (!gameLoopPrompt.matches("[nN]{1}")) {

            // prints title screen
            clear();
            System.out.printf("════════════════════════════════════════════════════════════\n%37s\n\nPoatz Software%46s\n════════════════════════════════════════════════════════════\n\n", "Cat Adventure", "2024-11-17 02:01:46");

            // initialize character attributes
            String charName = "";
            int charAge = 0;                            // has no effect on the game
            double charHeight = 0.0;                    // has no effect on the game
            int charHealth = 0;
            int charMaxHealth = 0;                      // character's health cannot go over this number
            String charWeapon = "";
            int charWeaponType = 0;                     // used purely for flavor text. 1 - melee weapon, 2 - ranged weapon
            int charAccuracy = 95;                      // the highest possible number used for guessing when fighting the boss
            int charAttempts = 0;                       // how many tries player takes to kill the boss. increments by 1 each time weapon is used

            String[] charInventory = new String[10];    // stores any items the player obtains during a playthrough
            Arrays.fill(charInventory, "");             // clears inventory
            boolean usingShield = false;                // used when fighting the boss


            // START OF CHARACTER CREATION
            String menuMainPrompt = controller("Type 1 to create a new character and start the game.\nType 2 to start the game with a pre-made character.\nTyping quit at any point will exit the program. ", "[12]{1}", "1 or 2");
            String menuCharPrompt = "";

            if (menuMainPrompt.equals("1")) {

                // loop for creating a character manually
                while (!menuCharPrompt.matches("[yY]{1}")) {
                    clear();
                    printGraphic(1);
                    log("");
                    charName = controller("Enter your character's name: ",".{0,25}","a name less than 25 characters");
                    charAge = Integer.parseInt(controller("Enter your character's age: ","\\d+","a number"));
                    charHeight = Double.parseDouble(controller("Enter your character's height: ","^-?\\d*\\.\\d+$","a number with a decimal (e.g. 5.7)"));
                    charWeapon = controller("Enter your character's weapon: ",".{0,23}","a weapon name less than 23 characters");
                    charWeaponType = Integer.parseInt(controller("Is your character's " + charWeapon + " a melee or ranged weapon? Enter 1 for melee or 2 for ranged: ","[1-2]{1}","1 or 2"));
                    charHealth = Integer.parseInt(controller("Enter your character's health (50 - 150, higher numbers make the game easier): ","(?:[5-9][0-9]|1[0-4][0-9]|150)","a number between 50-150"));
                    clear();
                    printGraphic(1);
                    log("");
                    System.out.printf("%-10s%s\n%-10s%s\n%-10s%s\n%-10s%s\n%-10s%s\n\n","Name:",charName,"Age:",charAge,"Height:",charHeight,"Weapon:",charWeapon,"Health:",charHealth);
                    menuCharPrompt = controller("Start a new game with this character? (Y/N) ","[yn]{1}","y or n");
                }

            } else {

                // creating a pre-made character from a text file
                while (!menuCharPrompt.matches("[yY]{1}")) {
                    try {
                        clear();
                        printGraphic(1);
                        log("");
                        String charFileName = searchForCharacter();
                        File file = new File(charFileName);
                        Scanner fileInput = new Scanner(file);

                        // loop that processes character text files
                        while (fileInput.hasNextLine()) {
                            String charLine = fileInput.nextLine();
                            String charValue = "";
                            String charTitle = "";
                            int dataRow = 0;
                            // breaks loop upon reaching the end of character file
                            if (charLine.equals("~")) { break; }

                            // assigns values to character based on the name of the stat in the file
                            if (charLine.contains(":")) {
                                dataRow = charLine.indexOf(":");
                                charTitle = charLine.substring(0, dataRow + 1).strip().toLowerCase();
                                charValue = charLine.substring(dataRow + 2).strip();
                                switch (charTitle) {
                                    case "name:":
                                        charName = charValue;
                                        break;
                                    case "age:":
                                        charAge = Integer.parseInt(charValue);
                                        break;
                                    case "height:":
                                        charHeight = Double.parseDouble(charValue);
                                        break;
                                    case "health:":
                                        charHealth = Integer.parseInt(charValue);
                                        break;
                                    case "weapon:":
                                        charWeapon = charValue;
                                        break;
                                    case "weapon type:":
                                        charWeaponType = Integer.parseInt(charValue);
                                        break;
                                }
                            }
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    clear();
                    printGraphic(1);
                    log("");
                    System.out.printf("%-10s%s\n%-10s%s\n%-10s%s\n%-10s%s\n%-10s%s\n\n","Name:",charName,"Age:",charAge,"Height:",charHeight,"Weapon:",charWeapon,"Health:",charHealth);
                    menuCharPrompt = controller("Start a new game with this character? (Y/N) ","[yn]{1}","y or n");
                }   // end of pre-made character loop
            }
            charMaxHealth = charHealth;
            // end character creation


            // START OF ROOM 1 - Player enters empty room, can use weapon to get an item
            clear();
            updateHUD(2, charName, charWeapon, charHealth, charMaxHealth);
            log(charName + " enters the dungeon's first room. There are crates \nscattered inside, and the door to the next room lies in \nfront. You can make " + charName + " act by typing in the \ncorresponding number next to the actions in the list.\n");
            String room1act1 = controller("1 - Open the door\n2 - Use weapon\n","[12]{1}","1 or 2");
            if (room1act1.equals("1")) {
                clear();
                updateHUD(2, charName, charWeapon, charHealth, charMaxHealth);
            } else if (room1act1.equals("2")) {
                clear();
                updateHUD(3, charName, charWeapon, charHealth, charMaxHealth);
                switch (charWeaponType){
                    case 1:
                        controller(charName + " used the " + charWeapon + " and swung around in every \ndirection, bashing the crates in the process.\n" + charName + " found a healing tonic in the rubble. (ENTER)", "", "");
                        break;
                    case 2:
                        controller(charName + " used the " + charWeapon + " and fired in \nseveral directions, destroying the crates in the process.\n" + charName + " found a healing tonic in the rubble. (ENTER)", "", "");
                        break;
                }
                charInventory = addInventory(charInventory, "Healing Tonic");
                clear();
                updateHUD(3, charName, charWeapon, charHealth, charMaxHealth);
            }
            controller(charName + " opens the door and proceeds to the next room. (ENTER)","","");
            // end of room 1


            // START OF ROOM 2 - Player tries to guess a number for rewards
            clear();
            charHealth = (int) (charHealth - (charMaxHealth * 0.15));
            updateHUD(4, charName, charWeapon, charHealth, charMaxHealth);
            controller(charName + " enters the dungeon's second room and slips on a \nbanana peel, losing " + (int) (charMaxHealth * 0.15) + " health from the fall. (ENTER)","","");
            clear();
            updateHUD(4, charName, charWeapon, charHealth, charMaxHealth);
            log("Upon recovering, " + charName + " notices a cat at the end \nof the room. \"If you can guess what number I'm thinking \nof, I'll give you a prize,\" it says.\n");

            // user guesses a random number with 10 attempts, gets reward based on how close they get
            switch (numberGuesser(5, 95, 10, "\"That wasn't it, but you were within 10 numbers away,\" says the cat.", "\"That wasn't it, but you were within 5 numbers away,\" says the cat.", "\"That's not it,\" says the cat.")) {

                case 3:
                    // if the player didn't guess close, they get no rewards
                    clear();
                    updateHUD(5, charName, charWeapon, charHealth, charMaxHealth);
                    controller("\"You're out of tries, too bad.\" The cat disappears. With \nnothing else to do, " + charName + " opens the door to the \nnext room. (ENTER)","","");
                    break;

                case 2:
                    // if the player guessed close, they get a shield
                    clear();
                    updateHUD(5, charName, charWeapon, charHealth, charMaxHealth);
                    controller("\"You're out of tries, but you got pretty close. Here, you \ncan have this thing I found.\" The cat gives " + charName + " \na shield before disappearing. With nothing else to do, \n" + charName + " opens the door to the next room. (ENTER)","","");
                    charInventory = addInventory(charInventory, "Shield");
                    break;

                case 1:
                    // if the player guessed correctly, they get a shield and guessing numbers during the boss fight is made easier
                    clear();
                    updateHUD(4, charName, charWeapon, charHealth, charMaxHealth);
                    controller("\"You got it on the nose. Let me see your " + charWeapon + " \nfor a second.\" The cat upgrades " + charName + "'s weapon, \nmaking it easier to land hits. (ENTER)","","");
                    charAccuracy = 50;
                    clear();
                    updateHUD(5, charName, charWeapon, charHealth, charMaxHealth);
                    controller("\"You can have this thing, too.\" The cat gives " + charName + " \na shield before suddenly disappearing. With nothing else to \ndo, " + charName + " opens the door to the next room. (ENTER)","","");
                    charInventory = addInventory(charInventory, "Shield");
                    break;
            }
            // end of room 2


            // START OF ROOM 3 - Boss fight
            clear();
            updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
            controller(charName + " enters the dungeon's final room. There is a \nmassive robot blocking the way to the exit. The robot \nsuddenly arms itself and begins to attack " + charName + ", \nbut it looks like there is a small window to act before the \nrobot does. (ENTER)","","");

            // initialize boss stats
            int enemyHealth = 2;                                                        // player must hit the boss (correctly guessing a number) twice to kill it
            int enemyState = 0;                                                         // Determines the boss' action. 0 - normal, 1 - charging special attack, 2 - stunned
            int damage = 0;                                                             // used for dealing damage to the player
            int guessTarget = 5 + (int) (Math.random() * ((charAccuracy - 5) + 1));     // the number the player must guess when using their weapon. changes each time the player guesses right
            boolean playerWon = false;                                                  // used for results screen

            //loop for fighting the boss. breaks if player dies or if the boss dies
            while (!(enemyHealth == 0)){
                clear();
                updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                String flavorText = "";

                // gathers all possible player actions based on what items the player has
                String[] actions = new String[charInventory.length + 1];
                actions[0] = charWeapon;
                int invCount = 1;
                for (String s : charInventory) {
                    if (!s.isEmpty()) {
                        actions[invCount] = s;
                        invCount++;
                    }
                }

                // player chooses an action
                log("What will " + charName + " do?\n");
                int action = 0;
                String actionInput = controllerCombat(actions);
                if (!actionInput.equals(charWeapon)){
                    switch (actionInput){
                        case "Healing Tonic":
                            action = 1;
                            break;
                        case "Shield":
                            action = 2;
                            break;
                    }
                }

                // carries out player's action
                switch (action){
                    case 0:
                        // weapon action (number guessing). prints out different flavor text based on what type of weapon player has
                        clear();
                        updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                        switch (numberGuesserCombat(guessTarget, charAccuracy)){
                            case 1:
                                log("You guessed correctly!");
                                controller("(ENTER)","","");
                                clear();
                                updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                                if (charWeaponType == 2){ log("∙ " + charName + " fired the " + charWeapon); } else { log("∙ " + charName + " took a swing with the " + charWeapon); }
                                if (charWeaponType == 2){ log("∙ " + charName + "'s shot hit the robot dead on"); } else { log("∙ " + charName + " landed a clean strike on the robot"); }
                                --enemyHealth;

                                // when the boss is hit, different flavor text is shown depending on the boss' health. the player must guess a different number each time the boss is hit
                                if (!(enemyHealth == 0)) {
                                    log("∙ " + "The robot looks like it can take one more hit");
                                    guessTarget = 5 + (int) (Math.random() * ((charAccuracy - 5) + 1));
                                } else {
                                    log("∙ " + "The robot was smashed to pieces");
                                }
                                break;

                            case 2:
                                log("You guessed within 5 numbers of the answer.");
                                controller("(ENTER)","","");
                                clear();
                                updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                                if (charWeaponType == 2){ log("∙ " + charName + " fired the " + charWeapon); } else { log("∙ " + charName + " took a swing with the " + charWeapon); }
                                if (charWeaponType == 2){ log("∙ " + charName + "'s shot barely grazed the robot"); } else { log("∙ " + charName + "'s attack bounced off the robot's armor"); }
                                break;

                            case 3:
                                log("You guessed within 10 numbers of the answer.");
                                controller("(ENTER)","","");
                                clear();
                                updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                                if (charWeaponType == 2){ log("∙ " + charName + " fired the " + charWeapon); } else { log("∙ " + charName + " took a swing with the " + charWeapon); }
                                if (charWeaponType == 2){ log("∙ " + charName + " barely missed the robot"); } else { log("∙ " + "The robot dodged " + charName + "'s attack"); }
                                break;

                            case 4:
                                log("Your guess was off by more than 10 numbers.");
                                controller("(ENTER)","","");
                                clear();
                                updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                                if (charWeaponType == 2){ log("∙ " + charName + " fired the " + charWeapon); } else { log("∙ " + charName + " took a swing with the " + charWeapon); }
                                if (charWeaponType == 2){ log("∙ " + charName + "'s shot completely missed"); } else { log("∙ " + charName + "'s attack missed"); }
                                break;

                        }
                        ++charAttempts;
                        break;

                    case 1:
                        // heal action - can only use if the player has a healing tonic. heals the player for 1/3 of their max health. removes healing tonic from the player's inventory
                        charHealth += (charMaxHealth / 3);
                        if (charHealth >= charMaxHealth) { charHealth = charMaxHealth; flavorText = "∙ " + charName + "'s health was maxed out"; } else { flavorText = "∙ " + charName + " recovered " + (charMaxHealth / 3) + " health"; }
                        clear();
                        updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                        log("∙ " + charName + " used a healing tonic");
                        log(flavorText);
                        removeInventory(charInventory, "Healing Tonic");
                        break;

                    case 2:
                        // shield action - can only use if player has a shield. protects the player for one turn. if the shield blocks the boss' special attack, the boss is stunned. the shield breaks if used too much
                        clear();
                        updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                        log("∙ " + charName + " raised up the shield");
                        usingShield = true;
                        break;
                }
                controller("(ENTER)","","");

                // ends the fight if the boss died
                if (enemyHealth == 0) { playerWon = true; break; }

                // boss chooses an action based on a dice roll: 1 - 10: basic attack, 11-15: waste turn, 16-20: charge special attack
                int enemyAI = (int) ((Math.random() * 20) + 1);
                int enemyAction = 0;
                if (enemyAI < 16) {
                    if (enemyAI < 11) {
                        enemyAction = 1;
                    }
                }   else {
                    enemyAction = 2;
                }

                // boss uses these actions instead if its state is not 0: 1 - use special attack, 2 - stunned for one turn
                switch (enemyState){

                    case 0:
                        // enemy is normal
                        break;

                    case 1:
                        // enemy special attack charged
                        enemyAction = 3;
                        enemyState = 0;
                        break;

                    case 2:
                        // enemy stunned
                        enemyAction = 4;
                        enemyState = 0;
                        break;
                }

                // carries out boss' action
                switch (enemyAction) {

                    case 0:
                        // wastes turn
                        if (usingShield) {
                            clear();
                            updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                            log("∙ The robot threw a punch at " + charName);
                            log("∙ " + charName + "'s shield blocked the attack");
                        } else {
                            clear();
                            updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                            log("∙ The robot threw a punch at " + charName);
                            log("∙ " + charName + " dodged the attack");
                        }
                        break;

                    case 1:
                        // basic attack - damages player for 10-25 health. does nothing if player is using the shield
                        if (usingShield) {
                            clear();
                            updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                            log("∙ The robot threw a punch at " + charName);
                            log("∙ " + charName + "'s shield blocked the attack");
                        } else {
                            damage = 10 + (int) (Math.random() * (25 - 10));
                            charHealth = damagePlayer(damage, charHealth);
                            clear();
                            updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                            log("∙ The robot threw a punch at " + charName);
                            log("∙ " + charName + " lost " + damage + " health from the blow");
                            if (charHealth == 0) { log("∙ " + charName + " was defeated"); }
                        }
                        break;

                    case 2:
                        // special attack - spends a turn charging up a big attack
                        clear();
                        updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                        log("∙ The robot began charging a laser beam");
                        enemyState = 1;
                        break;

                    case 3:
                        // special attack charged - if player is using the shield, the boss is stunned and the shield breaks. otherwise, the player loses 25-40 health
                        if (usingShield) {
                            clear();
                            updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                            log("∙ The robot fired a laser beam at " + charName);
                            log("∙ " + charName + "'s shield reflected the laser");
                            log("∙ The laser hit the robot, stunning it for one turn");
                            log("∙ " + charName + "'s shield broke");
                            removeInventory(charInventory, "Shield");
                            enemyState = 2;
                        } else {
                            damage = 25 + (int) (Math.random() * (40 - 25));
                            charHealth = damagePlayer(damage, charHealth);
                            clear();
                            updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                            log("∙ The robot fired a laser beam at " + charName);
                            log("∙ " + charName + " lost " + damage + " health from the attack");
                            if (charHealth == 0) { log("∙ " + charName + " was defeated"); }
                        }
                        break;

                    case 4:
                        // stunned - wastes a turn if player blocked the boss' special attack
                        clear();
                        updateHUD(6, charName, charWeapon, charHealth, charMaxHealth);
                        log("∙ The robot is stunned and cannot act");
                        enemyState = 0;
                        break;
                }
                controller("(ENTER)","","");

                //check player character's status. ends the fight if the player died
                usingShield = false;
                if (charHealth == 0) { break; }

            }   //end of boss loop
            //end of room 3

            // END RESULTS SCREEN - win / lose screen. writes player's results to results.txt. also saves their character file if they beat the boss
            clear();
            if (playerWon){
                printGraphic(7);
                log(charName + " managed to escape the dungeon, defeating the boss in \n" + charAttempts + " attempts. " + charName + "'s performance was saved to results.txt.\n");
                saveResults(true, charName, charAge, charHeight, charHealth, charMaxHealth, charWeapon, charWeaponType, charAttempts);
            } else {
                printGraphic(8);
                System.out.printf("Despite attacking the boss %s times, %s was defeated.\n", charAttempts, charName);
                log(charName + "'s performance was saved to results.txt.");
                saveResults(false, charName, charAge, charHeight, charHealth, charMaxHealth, charWeapon, charWeaponType, charAttempts);
            }

            // asks if player wants to try again. loops the game if player says Y
            gameLoopPrompt = controller("\nReturn to the title screen? (Y/N)","[yn]{1}","");

        }   // end game loop

    }   // end main


    /** Used at the end of a playthrough. Writes whether the player won or lost to results.txt, along with how many
     *  times they attacked the boss and how much health they had left upon winning. If the player wins, their
     *  character is saved to (name).txt, which can be imported for future playthroughs */
    public static void saveResults(boolean playerWon, String name, int age, double height, int health, int maxHealth, String weapon, int weaponType, int attempts ){
        try {
            PrintWriter resultFile = new PrintWriter(new FileWriter("result.txt", true));
            PrintWriter characterFile = new PrintWriter(new FileWriter(name.toLowerCase() + ".txt", false));
            double healthPercent = ((double) health / maxHealth) * 100;

            if (playerWon){
                resultFile.printf("∙ Player won the game using %s.\n  %s defeated the boss in %s attempts, with %.2f%% health remaining.\n\n", name, name, attempts, healthPercent);
                resultFile.flush();
                characterFile.printf("name: %s\nage: %s\nheight: %s\nhealth: %s\nweapon: %s\nweapon type: %s\n~", name, age, height, maxHealth, weapon, weaponType);
                characterFile.flush();
                System.out.println("For successfully completing the game, your character was \nsaved to " + name.toLowerCase() + ".txt. You can use this during \ncharacter creation by typing 2 at the title screen.");

            } else {
                resultFile.printf("∙ Player lost the game using %s.\n  %s attacked the boss %s times, but was ultimately defeated.\n\n", name, name, attempts);
                resultFile.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return;
    }


    /** Used during the boss fight. Damages player for a specified amount. Returns the health of the player after
     *  damage is applied. Returns 0 if health is negative */
    public static int damagePlayer( int damage, int charHealth) {
        charHealth -= damage;
        if (charHealth < 0) { charHealth = 0; }
        return charHealth;
    }


    /** Makes the player guess a number during the boss fight. Called when the player uses their weapon. Returns 1 if
     *  player guesses right, 2 if player guessed within 5 numbers, 3 if player guessed within 10 numbers, or 4 if
     *  player's guess wasn't close */
    public static int numberGuesserCombat(int guessTarget, int charAccuracy) {
        int result = 4;
        boolean guessedClose = false;

        // asks player to guess a number from 5 to 95 (or 50 if player guessed the number in the second level)
        int guess = Integer.parseInt(controllerGuess("To use your weapon, guess a number (" + 5 + " - " + charAccuracy + ") ", 5, charAccuracy,"a number between " + 5 + " and " + charAccuracy + "."));

        // returns 1 if player guessed correctly
        if (guess == guessTarget) {
            return 1;
        }

        // if player's guess is higher or lower than the target number, gives a hint (returns 2 or 3) based on how far away the player guessed
        if (guess > guessTarget) {
            if ((guess - guessTarget) <= 10) {
                if ((guess - guessTarget) <= 5) {
                    result = 2;
                } else {
                    result = 3;
                }
            }
        }
        if (guess < guessTarget) {
            if ((guessTarget - guess) <= 10) {
                if ((guessTarget - guess) <= 5) {
                    result = 2;
                } else {
                    result = 3;
                }
            }
        }

        // returns 4 if player's guess wasn't close
        return result;
    }


    /** Used in the second level. Makes the player guess a random number based on min and max parameters, giving them
     *  a specified amount of tries to do so. Returns a different number based on how close the player got to guessing
     *  correctly (1 - guessed right, 2 - guessed close, 3 - didn't guess close). Outputs different flavor text based
     *  on the player's guesses */
    public static int numberGuesser(int min, int max, int tries, String promptClose10, String promptClose5, String promptIncorrect) {
        int guessTarget = min + (int) (Math.random() * ((max - min) + 1));
        int result = 3;
        boolean guessedClose = false;

        // loop for guessing that breaks when player runs out of tries
        while (!(tries == 0)) {

            // asks for player's guess with different flavor text based on how many attempts they have left
            String triesString = "1 try";
            if (tries == 1) {
                triesString = "1 try";
            } else {
                triesString = tries + " tries";
            }
            int guess = Integer.parseInt(controllerGuess("You have " + triesString + " left. Guess a number (" + min + " - " + max + ") ", min, max,"a number between " + min + " and " + max + "."));

            // return 1 if the player guessed correctly
            if (guess == guessTarget) {
                return 1;
            }

            // gives a hint to the player if the guess was more or less than the target. gives a different hint depending on how far away the player guessed
            if (guess > guessTarget) {
                if ((guess - guessTarget) <= 10) {
                    if ((guess - guessTarget) <= 5) {
                        log(promptClose5);
                    } else {
                        log(promptClose10);
                    }
                    guessedClose = true;
                    --tries;
                } else {
                    log(promptIncorrect);
                    result = 3;
                    --tries;
                }
            }
            if (guess < guessTarget) {
                if ((guessTarget - guess) <= 10) {
                    if ((guessTarget - guess) <= 5) {
                        log(promptClose5);
                    } else {
                        log(promptClose10);
                    }
                    guessedClose = true;
                    --tries;
                } else {
                    log(promptIncorrect);
                    result = 3;
                    --tries;
                }
            }
        }

        // if the player runs out of tries but guessed close at any point, return 2. otherwise, return 3
        if (guessedClose) { return 2; }
        return result;
    }


    /** Removes an item from character's inventory */
    public static String[] removeInventory(String[] inventory, String item){
        int i = 0;
        while (i < inventory.length) {
            if (inventory[i].equals(item)) {
                inventory[i] = "";
            } else {
                i++;
            }
        }
        return inventory;
    }


    /** Adds an item to character's inventory */
    public static String[] addInventory(String[] inventory, String item){
        int i = 0;
        while (i < inventory.length) {
            if (inventory[i].isEmpty()) {
                inventory[i] = item;
                break;
            } else {
                i++;
            }
        }
        return inventory;
    }


    /** Method for importing character text files */
    public static String searchForCharacter() {
        String filePath = "";
        Scanner input = new Scanner(System.in);

        while (filePath.isEmpty()) {
            String fileName = controller("Enter the name of your character's text file: ",".*\\.(txt)$","a text file (e.g. name.txt)");

            File file = new File(System.getProperty("user.dir"));
            File[] files = file.listFiles();

            if (files != null) {
                for (File f : files) {
                    if (!f.isDirectory()) {
                        if (fileName.equals(f.getName())) {
                            filePath = f.getAbsolutePath();
                            break;
                        }
                    }
                }
            }
            if (filePath.isEmpty()) { log("The character's file was not found."); }
        }
        return filePath;
    }


    /** Method for importing the graphics file. If the file is missing, the program quits */
    public static String searchGraphic(){
        String pathName = "";
        String fileName = "graphics.txt";

        //gets array of files inside root directory
        File file = new File(System.getProperty("user.dir"));
        File[] files = file.listFiles();

        //checks each file in array for the searched file
        //if the searched file is found, return the file's path
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory()) {
                    if (fileName.equals(f.getName())) {
                        pathName = f.getAbsolutePath();
                        break;
                    }
                }
            }
        }
        if (pathName.isEmpty()) {
            log("The graphics file was not found. Make sure graphics.txt is in the program folder. The program will exit");
            System.exit(-1);
        }
        return pathName;
    }


    /** Prints ascii art from graphics.txt based on the graphic parameter. */
    public static void printGraphic(int graphic) {
        try {
            // reads in graphics file
            File graphicsFile = new File(searchGraphic());
            Scanner graphicsInput = new Scanner(graphicsFile);
            int graphicCount = 0;

            // loop that processes graphics file
            while (graphicsInput.hasNextLine()) {
                String graphicsText = graphicsInput.nextLine();
                // each graphic is separated by a ~ in the text file, which is used as an index
                if (graphicsText.equals("~")) {
                    graphicCount++;
                }

                // if the right ~ is found, prints everything on each line after that until it finds another ~
                if (graphicCount == graphic) {
                    graphicsText = graphicsInput.nextLine();
                    while (!graphicsText.equals("~")) {
                        log(graphicsText);
                        graphicsText = graphicsInput.nextLine();
                    }
                    break;
                }   // end if
            }   // end while

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /** Prints ascii art from graphics.txt based on the graphic parameter before printing a box containing character's
     *  stats. Typically used in conjunction with clear() to give the illusion of a hud */
    public static void updateHUD(int graphic, String name, String weapon, int health, int maxHealth) {
        printGraphic(graphic);
        System.out.println("╔═NAME═════════════════════WEAPON═══════════════════HEALTH═╗");
        System.out.printf("║ %-25s%-23s%8s ║\n", name, weapon, health + "/" + maxHealth);
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }


    /** Clears screen in command prompt */
    public static void clear(){
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    /** Shortened version of println */
    public static void log (String message) { System.out.println(message); }


    /** A variation of controller(). Used during the boss fight each time the player has a turn. Returns the name of
     *  the action chosen by the player. Checks for valid input by using actions parameter */
    public static String controllerCombat(String[] actions) {
        boolean checkInput = false;
        Scanner input = new Scanner(System.in);
        String in = "";
        int inCheck = 0;

        // loop for checking player's input. breaks if input is valid
        while (!checkInput) {

            // prints a list of possible actions the player can use before getting input
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] != null){
                    log(i + " - use " + actions[i]);
                }
            }
            in = input.nextLine();

            // if player's input is null, prompt the player to try again
            if (in == ""){
                System.out.println("Invalid input: You must enter an action's number");
            } else {
                // if player's input matches an action's number, return the name of the action
                if (in.matches("[0-9]?")) {
                    inCheck = Integer.parseInt(in);
                    if (inCheck <= actions.length) {
                        checkInput = true;
                    } else {
                        System.out.println("Invalid input: You must enter an action's number");
                    }
                } else {
                    if (in.equalsIgnoreCase("quit")) {
                        System.exit(-1);
                    }
                    System.out.println("Invalid input: You must enter an action's number");
                }
            }
        }

        return actions[inCheck];
    }


    /** A variation of controller(). Used for getting input to guess a number. Checks for valid input based on min and
     *  max parameters */
    public static String controllerGuess(String prompt, int min, int max, String errorPrompt) {
        boolean checkInput = false;
        Scanner input = new Scanner(System.in);
        String in = "";
        String inCheck = "";

        // loop for checking player's input. breaks if input is valid
        while (!checkInput) {
            System.out.print(prompt);
            in = input.nextLine();
            inCheck = in.toLowerCase();

            // returns the player's input if it matches a number from min to max parameters
            if (inCheck.matches("[0-9][0-9]?") && (Integer.parseInt(inCheck)) >= min && Integer.parseInt(inCheck) <= max) {
                checkInput = true;
            } else {

                // if input doesn't match "quit" or a number from min to max, the player is prompted to try again
                switch (inCheck) {
                    case "quit":
                        System.exit(-1);
                        break;
                    default:
                        System.out.println("Invalid input: You must enter " + errorPrompt);
                        break;
                }
            }
        }   //end while

        return in;
    }


    /** Game controller - Prompts player and returns their input if it is valid. If player's input is invalid, they are
     *  prompted again. Allows player to quit the program at any point by typing "quit" */
    public static String controller(String prompt, String desiredInput, String errorPrompt) {
        boolean checkInput = false;
        Scanner input = new Scanner(System.in);
        String in = "";
        String inCheck = "";

        // loop for checking player's input. breaks if input is valid
        while (!checkInput) {
            System.out.print(prompt);
            in = input.nextLine();
            inCheck = in.toLowerCase();

            // returns the player's input if it matches what is specified in desiredInput
            if (inCheck.matches(desiredInput)) {
                checkInput = true;
            } else {

                // if input doesn't match "quit" or desiredInput, prints errorPrompt and lets player try again
                switch (inCheck) {
                    case "quit":
                        System.exit(-1);
                        break;
                    default:
                        System.out.println("Invalid input: You must enter " + errorPrompt);
                        break;
                }
            }
        }   // end while

        return in;
    }   // end method


}   // end class