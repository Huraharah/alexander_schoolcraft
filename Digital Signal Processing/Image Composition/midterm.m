function midterm(imageFile1, imageFile2, scale, margin)
    background = imread(imageFile1);
    foreground = imread(imageFile2);
    resized_foreground = imresize(foreground, scale);
    
    [bg_height, bg_width, ~] = size(background);
    [fg_height, fg_width, ~] = size(resized_foreground);
    total_fg_height = fg_height + 2 * margin;
    total_fg_width = fg_width + 2 * margin;
    margin_image = 255 * ones(total_fg_height, total_fg_width, 3, 'uint8');
    margin_image(margin+1:margin+fg_height, margin+1:margin+fg_width, :) = resized_foreground;

    top_left_y = round((bg_height - total_fg_height) / 2);
    top_left_x = round((bg_width - total_fg_width) / 2);

    combined_image = background;
    
    combined_image(top_left_y+1:top_left_y+total_fg_height, top_left_x+1:top_left_x+total_fg_width, :) = margin_image;

    imshow(combined_image);
end
