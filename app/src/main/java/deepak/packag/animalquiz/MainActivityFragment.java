package deepak.packag.animalquiz;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainActivityFragment extends Fragment {

    private static final int NUMBERS_OF_ANIMALS_INCLUDED_IN_QUIZ = 10;

    //List are like array and we can assign array to the list, contain all animal names inside assets folder.
    private List<String> allAnimalsNameList;
    //This animalsNamesQuizList will gonna hold randomly selected images from assets folder.
    private List<String> animalsNamesQuizList;
    //Set is like a list but we cannot assign duplicate value in it,it will hold the type of animal, wild or tame
    //can assign array
    private Set<String> animalTypesInQuiz;
    //It will store the correct guesses of user
    private String correctAnimalsAnswer;
    private int numberOfAllGuesses;
    private int numberOfRightAnswers;
    private int numberofAnimalsGuessRows;
    private SecureRandom secureRandomNumber;
    //It will help to transition to next question
    private Handler handler;
    private Animation wrongAnswerAnimation;

    //We can change the background color
    private LinearLayout animalQuizLinearLayout;
    private TextView txtQuestionNumber;
    private ImageView imgAnimal;
    private LinearLayout[] rowsOfGuessButtonsInAnimalQuiz;
    private TextView txtAnswer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_main, container, false);

        allAnimalsNameList = new ArrayList<>();
        animalsNamesQuizList = new ArrayList<>();
        secureRandomNumber = new SecureRandom();
        handler = new Handler();

        wrongAnswerAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.wrong_answer_animation);
        //It will perform the animation effect for only once
        wrongAnswerAnimation.setRepeatCount(1);

        //This is background linear layout to change the colors
        animalQuizLinearLayout = view.findViewById(R.id.animalQuizLinearLayout);

        txtQuestionNumber = view.findViewById(R.id.txtQuestionNumber);
        imgAnimal = view.findViewById(R.id.imgAnimal);
        rowsOfGuessButtonsInAnimalQuiz = new LinearLayout[3];
        rowsOfGuessButtonsInAnimalQuiz[0] = view.findViewById(R.id.firstRowLinearLayout);
        rowsOfGuessButtonsInAnimalQuiz[1] = view.findViewById(R.id.secondRowLinearLayout);
        rowsOfGuessButtonsInAnimalQuiz[2] = view.findViewById(R.id.thirdRowLinearLayout);
        txtAnswer = view.findViewById(R.id.txtAnswer);

        //Here we are using for loop to get buttons inside each linear layouts by using indexes
        //After getting buttons We are making all buttons a listener by their indexes which are in column
        //Because rowsOfGuessButtonsInAnimalQuiz data type is linear layout
        for (LinearLayout row: rowsOfGuessButtonsInAnimalQuiz){

            //get child means linear layout always have a child, we can say that buttons or object
            for (int column = 0; column < row.getChildCount();column++){

                //Here column show the value as it has indexes of all elements.
                Button btnGuess = (Button) row.getChildAt(column);
                btnGuess.setOnClickListener(btnGuessListener);
                btnGuess.setTextSize(24);
            }
        }

        //For arguments check string resources
        //Wheneve the app run it will show 1 of 10 as txtQuestionNumber
        txtQuestionNumber.setText(getString(R.string.question_text,1,NUMBERS_OF_ANIMALS_INCLUDED_IN_QUIZ));
        return view;

    }

    //When user tapped the button, this method will be executed
    private View.OnClickListener btnGuessListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Button btnGuess = ((Button) view);

            //Here we are getting the text of the button whatever will be tapped by user.
            String guessValue = btnGuess.getText().toString();
            String answerValue = getTheExactAnimalName(correctAnimalsAnswer);
            ++numberOfAllGuesses;

            //If the answer is correct
            if (guessValue.equals(answerValue)){
                //It user touches the right button it will increment by 1, it has a record of right answers
                ++numberOfRightAnswers;

                //This answer value containing the right animal name
                txtAnswer.setText(answerValue + "!" + "RIGHT");

                //After tapping on right answer button rest buttons will be disabled.
                disableQuizGuessButtons();

                // if 10 out of 10 right answers , reset the game dialog fragment
                if (numberOfRightAnswers == NUMBERS_OF_ANIMALS_INCLUDED_IN_QUIZ){

                    DialogFragment animalQuizResults = new DialogFragment(){

                        @NonNull
                        @Override
                        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            builder.setMessage(getString(R.string.results_string_value,numberOfAllGuesses,(1000 / numberOfAllGuesses)));

                            builder.setPositiveButton(R.string.reset_animal_quiz, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    resetAnimalQuiz();
                                }
                            });

                            return builder.create();

                        }
                    };

                    //User has to tap on the alert dialog to reset the game , it they touches anywhere else it wont work
                    animalQuizResults.setCancelable(false);
                    animalQuizResults.show(getFragmentManager(),"AnimalQuizResult");


                    //If the quiz is not finished yet
                    //After 1 second it will show the next question as per the method inside
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            animateAnimalQuiz(true);

                        }
                        //This method will execute after 1 second of right answer
                    },1000);

                }

                //if the answer is not correct
            }else {

                imgAnimal.setAnimation(wrongAnswerAnimation);
                txtAnswer.setText(R.string.wrong_answer_message);
                //It means the button listener will be disabled, we can't touch the same button after wrong answer
                btnGuess.setEnabled(false);
            }
        }
    };

    //This will start the image name reading after - this and replace _ to '', which are in assets folder and show the name on the screen
    //Getting exact name from the image name
    private String getTheExactAnimalName (String animalName){

        return animalName.substring(animalName.indexOf('-') + 1).replace('-',' ');
    }

    //This method is for making all buttons disabled
    private void disableQuizGuessButtons (){

        for (int row = 0; row < numberofAnimalsGuessRows;row++){

            LinearLayout guessRowLinearLayout = rowsOfGuessButtonsInAnimalQuiz[row];

            for (int buttonIndex = 0; buttonIndex < guessRowLinearLayout.getChildCount();buttonIndex++){

                guessRowLinearLayout.getChildAt(buttonIndex).setEnabled(false);

            }
        }
    }

    //If user change any setting like fonts, color, it will restart the quiz
    //Accessing all images in the assets folder,Getting random index of images.
    public void resetAnimalQuiz(){

        //To access the assets folder
        AssetManager assets = getActivity().getAssets();
        //Because we are reset the quiz, all the name inside the allAnimalsNameList arraylist is cleared.
        allAnimalsNameList.clear();

        try {
            //animalTypesInQuiz data is set, its avoid duplicate pictures
            //animalTypesInQuiz is responsible to hold the animalType like wild animals or tame animals name and images
            // animalTypesInQuiz is empty as of now
            for (String animalType: animalTypesInQuiz){

               //Accessing the folder inside assets folder, wild animal or tame animal
                //Later we will assign the type of animal in the animalTypesInQuiz and that will be hold by the animalType
                String[] animalImagePathsInQuiz = assets.list(animalType);

                //Here we are getting all images inside the tame animal or wild animals, whatever will be selected by user
                for (String animalImagePathInQuiz : animalImagePathsInQuiz){
                    //Adding all the images in the allAnimalsNameList arraylist and replacing .png from image names
                    allAnimalsNameList.add(animalImagePathInQuiz.replace(".png",""));

                }
            }

        }catch (IOException e){

            Log.e("AnimalQuiz","Error",e);
        }

        //Because the game is reset, it will be empty.
        numberOfRightAnswers = 0;

        numberOfAllGuesses = 0;
        animalsNamesQuizList.clear();

        int counter = 1;

        //allAnimalsNameList is an array and has all the images inside which are inside the assets folder according to animalType
        //Check forloop above
        int numberOfAvailableAnimals = allAnimalsNameList.size();

        //as NUMBERS_OF_ANIMALS_INCLUDED_IN_QUIZ value is 10, it will execute 10 times
        while (counter <=NUMBERS_OF_ANIMALS_INCLUDED_IN_QUIZ){

            //It will show the random images which are in numberOfAvailableAnimals
            int randomIndex = secureRandomNumber.nextInt(numberOfAvailableAnimals);
            //assigining the random index to the image arrayList
            String animalImageName = allAnimalsNameList.get(randomIndex);

            //It will avoid the repeating animalImage in the quiz, everytime a different question

            if (!animalsNamesQuizList.contains(animalImageName)){

                //animalsNamesQuizList arraylist now hold the random index images
                animalsNamesQuizList.add(animalImageName);

                //After each forloop it will add by 1 till number 10 because we have to show only 10 questions to show
                ++counter;
            }
        }

        showNextAnimal();
    }

    //This animation will perform after giving the right answer
    private void animateAnimalQuiz(boolean animateOutAnimalImage){

        //If there is no right answer inside numberOfRightAnswers, it will not execute the below codes
        if (numberOfRightAnswers == 0){

            return;
        }

        int xTopLeft = 0;
        int yTopLeft = 0;

        //animalQuizLinearLayout is a parent linear layout
        int xBottomRight = animalQuizLinearLayout.getLeft() + animalQuizLinearLayout.getRight();
        int yBottomRight = animalQuizLinearLayout.getTop() + animalQuizLinearLayout.getBottom();

        //Screen width and height, get the maximum value b/w both the width and height, whichever is higher
        int radius = Math.max(animalQuizLinearLayout.getWidth(),animalQuizLinearLayout.getHeight());

        //It is required to perform an animation
        Animator animator;

        //If the value is true it will execute below lines
        if (animateOutAnimalImage){

            animator = ViewAnimationUtils.createCircularReveal(animalQuizLinearLayout,xBottomRight,yBottomRight,radius,0);

            //We need to show the next animal image after animation completed
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {

                    showNextAnimal();

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });


        } else { //It will happen when next image shown to the user

            animator = ViewAnimationUtils.createCircularReveal(animalQuizLinearLayout,xTopLeft,yTopLeft,0,radius);

        }

        animator.setDuration(700);
        animator.start();
    }

    //This method use for next image with options buttons
    private void showNextAnimal(){

        //nextAnimalImageName will hold the next image which we are going to the user
        //By using the index we now what will be the next image will be shown
        String nextAnimalImageName = animalsNamesQuizList.remove(0);
        //If there is a correct answer, that specific index value will assign it to correctAnimalsAnswer below
        correctAnimalsAnswer = nextAnimalImageName;
        txtAnswer.setText("");

        //It will add 1 value to the question number
        txtQuestionNumber.setText(getString(R.string.question_text,numberOfRightAnswers + 1,NUMBERS_OF_ANIMALS_INCLUDED_IN_QUIZ));
        String animalType = nextAnimalImageName.substring(0,nextAnimalImageName.indexOf("-"));

        //Again we are accessing the assets folder to access the images for next question
        AssetManager assets = getActivity().getAssets();

        //It is important to mention .png because in the animalsNamesQuizList we have removed .png
        //It will load the next image on the emulator from the assets folder
        try (InputStream stream = assets.open( animalType + "/" + nextAnimalImageName + ".png")) {

            Drawable animalImage = Drawable.createFromStream(stream,nextAnimalImageName);
            imgAnimal.setImageDrawable(animalImage);
            //When the next image will appear, this animation will perform.
            animateAnimalQuiz(false);

        }catch (IOException e){

            Log.e("Animal Quiz","There is an Error Getting" + nextAnimalImageName,e);
        }

        Collections.shuffle(allAnimalsNameList);

        //This line means, it holds the answer of the current question because we have already taken the next question index from the animalsNamesQuizList arraylist
        // and assign to correctAnimalAnswer
        int correctAnimalNameIndex = allAnimalsNameList.indexOf(correctAnimalsAnswer);
        //Removing the correct answer from the list
        String correctAnimalName = allAnimalsNameList.remove(correctAnimalNameIndex);
        //Adding the correct answer in the list in the last index
        allAnimalsNameList.add(correctAnimalName);

        //Gettting all the buttons and making them enables
        for (int row= 0; row < numberofAnimalsGuessRows;row++){

            for (int column = 0;column < rowsOfGuessButtonsInAnimalQuiz[row].getChildCount(); column++){

                Button btnGuess = (Button) rowsOfGuessButtonsInAnimalQuiz[row].getChildAt(column);

                btnGuess.setEnabled(true);

                //It will give the name of the image by the index number
                String animalImageName = allAnimalsNameList.get((row * 2) + column);
                //By using getTheExactAnimalName method we remove the unwanted part from the image name and put that name on the button on the emulator
                btnGuess.setText(getTheExactAnimalName(animalImageName));

            }
        }

        //secureRandomNumber generates random nmbrs AND numberOfAnimalsGuessRows
        // shows number of animal guess rows
        //Here substituting one of the guess options with correct answer
        //It will generate the random number of guess rows
        int row = secureRandomNumber.nextInt(numberofAnimalsGuessRows);

        //It will generate the random number of column
        int column = secureRandomNumber.nextInt(2);

        //random index number of the guess row
        LinearLayout randomRow = rowsOfGuessButtonsInAnimalQuiz[row];

        //Putting the correct answer on the button
        String correctAnswerImageName = getTheExactAnimalName(correctAnimalName);

        //Setting one button as a correct answer
        ((Button) randomRow.getChildAt(column)).setText(correctAnswerImageName);

    }

    //This method will be called when there is change in the guess options settings
    public void modifyAnimalGuessRows (SharedPreferences sharedPreferences){

        //GUESSES is the key of list preferences of guess options in xml file quiz_preferences
        //Whenever there is a change in the settings shared preference will save that change because in the file persistant is true, we get the same
        final String NUMBER_OF_GUESS_OPTIONS = sharedPreferences.getString(MainActivity.GUESSES,null);

        //According to the guess options selections rows will be assign, we get the selection from sharedPreference
        numberofAnimalsGuessRows = Integer.parseInt(NUMBER_OF_GUESS_OPTIONS) / 2;

        //When there is change in the settings , remove all the previous rows
        for (LinearLayout horizontalLinearLayout : rowsOfGuessButtonsInAnimalQuiz){

            horizontalLinearLayout.setVisibility(View.GONE);
        }

        //New rows will be shown according to the guess options settings
        for (int row = 0; row < numberofAnimalsGuessRows;row++){

            rowsOfGuessButtonsInAnimalQuiz[row].setVisibility(View.VISIBLE);
        }

    }

    //This method will be called when there is change in the Animal type options settings
    public void modifyTypeOfAnimalInQuiz (SharedPreferences sharedPreferences){

        //Whenever there is a change in the settings shared preference will save that change because in the file persistant is true, we get the same
        animalTypesInQuiz = sharedPreferences.getStringSet(MainActivity.ANIMAL_TYPE,null);

    }

    //This method will be called when there is change in the fonts options settings
    public void modifyQuizFont (SharedPreferences sharedPreferences){

        //This fontStringValue now has all three fonts
        String fontStringValue = sharedPreferences.getString(MainActivity.QUIZ_FONT,null);

        switch (fontStringValue){

            case "Chunkfive.otf":


                for (LinearLayout row : rowsOfGuessButtonsInAnimalQuiz){

                    for (int column =0;column<row.getChildCount();column++){

                        Button button = (Button) row.getChildAt(column);
                        button.setTypeface(MainActivity.chunkfive);
                    }
                }

                break;

            case "FontleroyBrown.ttf":

                for (LinearLayout row : rowsOfGuessButtonsInAnimalQuiz){

                    for (int column = 0; column < row.getChildCount(); column++){

                        Button button = (Button) row.getChildAt(column);
                        button.setTypeface(MainActivity.fontlerybrown);
                    }
                }

                break;

            case "Wonderbar Demo.otf":

                for (LinearLayout row : rowsOfGuessButtonsInAnimalQuiz){

                    for (int column =0;column < row.getChildCount();column++){

                        Button button = (Button) row.getChildAt(column);
                        button.setTypeface(MainActivity.wonderbarDemo);
                    }
                }

                break;

        }

    }

    //This method will be called when there is change in the fonts options settings
    public  void modifyBackgroundColor(SharedPreferences sharedPreferences){

        //This backgroundColor now has all three fonts
        String backgroundColor = sharedPreferences.getString(MainActivity.QUIZ_BACKGROUND_COLOR,null);

        switch (backgroundColor){

            case "White":

                animalQuizLinearLayout.setBackgroundColor(Color.WHITE);

                for (LinearLayout row:rowsOfGuessButtonsInAnimalQuiz){

                    for (int column=0; column<row.getChildCount();column++){

                        Button button = (Button) row.getChildAt(column);
                        button.setTextColor(Color.WHITE);
                        button.setBackgroundColor(Color.BLUE);

                    }
                }

                txtAnswer.setTextColor(Color.BLUE);
                txtQuestionNumber.setTextColor(Color.BLACK);

                break;

            case "Black":

                animalQuizLinearLayout.setBackgroundColor(Color.BLACK);

                for (LinearLayout row:rowsOfGuessButtonsInAnimalQuiz){

                    for (int column=0;column < row.getChildCount();column++){

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);
                    }
                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.WHITE);

                break;

            case "Green":

                animalQuizLinearLayout.setBackgroundColor(Color.GREEN);

                for (LinearLayout row:rowsOfGuessButtonsInAnimalQuiz){

                    for (int column=0;column<row.getChildCount();column++){

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);
                    }
                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.YELLOW);

                break;

            case "Blue":

                animalQuizLinearLayout.setBackgroundColor(Color.BLUE);

                for (LinearLayout row: rowsOfGuessButtonsInAnimalQuiz){

                    for (int column = 0;column< row.getChildCount(); column++){

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.RED);
                        button.setTextColor(Color.WHITE);
                    }

                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.WHITE);
                break;

            case "Red":

                animalQuizLinearLayout.setBackgroundColor(Color.RED);

                for (LinearLayout row :rowsOfGuessButtonsInAnimalQuiz){

                    for (int column=0;column<row.getChildCount(); column++){

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);
                    }
                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.WHITE);

                break;

            case "Yellow":

                animalQuizLinearLayout.setBackgroundColor(Color.YELLOW);

                for (LinearLayout row:rowsOfGuessButtonsInAnimalQuiz){

                    for (int column=0;column<row.getChildCount() ; column++){

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLACK);
                        button.setTextColor(Color.WHITE);
                    }
                }

                txtAnswer.setTextColor(Color.BLACK);
                txtQuestionNumber.setTextColor(Color.BLACK);

                break;
        }
    }


}



