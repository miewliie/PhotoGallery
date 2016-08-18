package com.augmentis.ayp.photogallery;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by Apinya on 8/16/2016.
 */
public class GalleryItemTest {

    private GalleryItem mGalleryItem;

    @Before
    public void preparation(){ //this method will called every time that have method test
        mGalleryItem = new GalleryItem();
        System.out.println("init gallery");
    }

    @Test
    public void testNew_instance(){
        //Assertion
        assertThat(mGalleryItem, notNullValue());
    }

    @Test
    public void test_setter_and_getter_then_correct(){
        GalleryItem b = new GalleryItem();

        mGalleryItem.setId("1234");
        mGalleryItem.setTitle("Hello");
        mGalleryItem.setUrl("http://www.facebook.com");

        b.setId("4321");

        assertThat(mGalleryItem.getId(), is("1234"));
        assertThat(mGalleryItem.getTitle(), is("Hello"));
        assertThat(mGalleryItem.getUrl(), is("http://www.facebook.com"));

        assertThat(b.getId(), is("4321"));
        assertThat(b.getId(), not("1234"));
        assertThat(b.getUrl(), nullValue());
    }


    @Test
    public void test_set_title_then_get_name(){
        mGalleryItem.setTitle("Title1");

        assertThat(mGalleryItem.getName(), is("Title1"));
    }

    @Test
    public void test_equals(){
        GalleryItem targetGalleryItem = new GalleryItem();

        mGalleryItem.setId("1");
        targetGalleryItem.setId("1");

        assertThat(mGalleryItem.equals(targetGalleryItem), is(Boolean.TRUE));
    }

    @Test
    public void test_not_equals(){
        GalleryItem targetGalleryItem = new GalleryItem();

        mGalleryItem.setId("2");
        targetGalleryItem.setId("1");

        assertThat(mGalleryItem.equals(targetGalleryItem), is(Boolean.FALSE));
    }

}