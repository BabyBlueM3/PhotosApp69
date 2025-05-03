Group 69, Project 4: Android Photo Viewer

by Jack Loyd and Will Tran

--{AI Usage}--
Jack Loyd:
I mainly used Claude to design the UI. I had Claude generate each activity based off of a description, and then tweaked the design myself to perfect it. 

[Home Activity]
This activity was the first thing I prompted. Since I suck at drawing, I opted to instead describe exactly what I wanted.
This was my prompt: 

	"in android studio, i need to create a ui for the homescreen of an image viewer app. it should be using sdk 34 and be designed for the pixel 6 phone. all i need is a search bar at the top, and below it a scrollable list of albums (that will be created dynamically by my code). please write the activity_home.xml file as well as place at least 1 placeholder album."

The results were suprisingly good! Maybe due to the Android UI elements having a very clean theme to them by default, I was suprised at how nice and professional it looked. After integrating this Claude generated code with the already existing code from project 3, I prompted: 

	"ok, now i want to add 4 buttons to the bottom labeled from left to right: 
	Open
	Rename
	Delete
	Edit
	Create"

Unlike the first prompt, I had to change the button styles a lot. Claude created 4 functional buttons, but they had really small white text on a white background. The buttons were pretty much invisible. Claude also decided to go ahead and create listeners on the albums in the list for the button functionality, which was nice because that was going to be my next prompt anyways!

After getting that working, I reset the context and message limits by logging into a new Google account to prepare for the next activity.

[Album Activity]
This is the activity that opens when you open an album or when you search.
My first prompt:

	"in android studio, i need to create a ui for the album viewer of an image viewer app. essentially, on my home activity the user can select an album to open, and this activity contains the album name at the top (as a title), a list of photo thumbnails that can scroll if needed (please make it 4 images wide), and 4 buttons on the bottom for functionality. the photos should be selectable, and I have included code to format these buttons correctly, i just need you to integrate them properly with the rest of the ui. please write the activity_album.xml file as well as place at least 1 placeholder photo."

I also provided this code from the previous activity for the bottom buttons to keep their design consistent (this included my tweaks to the style as well):

<!-- Bottom Buttons Container -->
<LinearLayout
    android:id="@+id/buttons_container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:background="#FFFFFF"
    android:elevation="8dp"
    android:padding="8dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent">

    <!-- Open Button -->
    <Button
        android:id="@+id/btn_open"
        style="@style/Widget.MaterialComponents.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginEnd="4dp"
        android:layout_weight="1"
        android:text="Open"
        android:textAppearance="@style/TextAppearance.AppCompat.Medium"
        android:textColor="#0F0C0C"
        android:textSize="16sp" />

    <!-- Rename Button -->
    <Button
        android:id="@+id/btn_rename"
        style="@style/Widget.MaterialComponents.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="4dp"
        android:layout_marginEnd="4dp"
        android:layout_weight="1"
        android:text="Rename"
        android:textAppearance="@style/TextAppearance.AppCompat.Medium"
        android:textColor="#121010"
        android:textSize="16sp" />

    <!-- Delete Button -->
    <Button
        android:id="@+id/btn_delete"
        style="@style/Widget.MaterialComponents.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="4dp"
        android:layout_marginEnd="4dp"
        android:layout_weight="1"
        android:text="Delete"
        android:textAppearance="@style/TextAppearance.AppCompat.Medium"
        android:textColor="#020000"
        android:textSize="16sp" />

    <!-- Create Button -->
    <Button
        android:id="@+id/btn_create"
        style="@style/Widget.MaterialComponents.Button.OutlinedButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="4dp"
        android:layout_weight="1"
        android:text="Create"
        android:textAppearance="@style/TextAppearance.AppCompat.Medium"
        android:textColor="#000000"
        android:textSize="16sp" />
</LinearLayout>

Claude decided to go above and beyond once again, and made it so the images opened by tapping and were selected (with multiple selections) on long press. My intended functionality was to have a tap select a single image at a time because the buttons at the bottom are meant for a single image.
Because the last prompt ran out of context, I had to start a new chat and input the 2 files required along with this prompt:

	"so i need to change how this works. id like it to only select a single photo at a time. also, tapping a photo should select it instead of opening it (there is an open button at the bottom for this functionality already). in addition, there should be no long press functionality (because selecting a photo is moved to just tapping)"

I like to talk to AI like it is a human, and give it as much context as I would expect another person to need. Claude does such a wonderful job at paying attention to every little detail. I noticed that whenever I provided any files to it, it made sure the package was set up correctly (or close to it), as well as making sure any provided classes were correctly named.
This refined the functionality, and with just some more manual tweaks to remove some relics of the multi selection (the check boxes in the corners of each image, as well as the selection indicator in the bottom left) the activity's UI was done. One more to go!

[View Activity]
This activity is the image viewer. It has the remaining functionality of the app, namely:
1) the image displayed at the top
2) both tags listed below (with their corresponding edit buttons)
3) left and right buttons at the bottom to scroll between images in an album. aka the slideshow function.

I started by prompting for the first 2: 

	"in android studio, i need to create a ui for an image viewer app. it should be using sdk 34 and be designed for the pixel 6 phone. the activity should be the screen that opens when you select an image from an album, that displays the photo in the top section of the screen, as well as have space for the tags. there are only 2 tags: Person and Location. id like them to be listed one above the other, and next to each there should be an edit tag button. please write the activity_view.xml file."

This time, Claude decided to only create the activity_view.xml file (which is actually what I was intending for the previous two activities, I just wasn't strict) so after it finished, I asked it to also make the controller class with this prompt:

	"i need the ViewActivity class"

It got right to work, and also created the edit tag dialogue box for me as well. 
For #3, I decided to just make them myself. Was pretty simple, although I had to figure out that the layout had to be constrained on both axis, not just to the bottom. 

Update: I forgot to keep updating this. I ended up having to use 4 different google accounts just to fit what i needed in the contexts/free message limits
