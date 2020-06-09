package de.bamberg.uni.isosysc.dsg.shared.models;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
/**
 * 
 * @author amit
 *
 */
/*
 * To provide pagination of various resources.
 */
public class Pagination {
	
	public static Page<WastageBean> createPageFromList(List<WastageBean> list, Pageable pageable)
	{
		int start = 0;
		int end = list.size();
		
		if(pageable.isPaged())
		{
			start = (int) pageable.getOffset();
			end = (int) ((start + pageable.getPageSize()) > list.size() ? list.size()
				  : (start + pageable.getPageSize()));
		}

		if(!(list.subList(start, end).isEmpty()))
			return new PageImpl<WastageBean>(list.subList(start, end), pageable, list.size());
		else
			return null;
	}
	
	public static Page<OfferBean> createPageFromOfferList(List<OfferBean> list, Pageable pageable)
	{
		int start = 0;
		int end = list.size();
		
		if(pageable.isPaged())
		{
			start = (int) pageable.getOffset();
			end = (int) ((start + pageable.getPageSize()) > list.size() ? list.size()
				  : (start + pageable.getPageSize()));
		}
		
		if(!(list.subList(start, end).isEmpty()))
			return new PageImpl<OfferBean>(list.subList(start, end), pageable, list.size());
		else
			return null;
	}
	
	public static Page<ImageBean> createPageFromImageList(List<ImageBean> list, Pageable pageable)
	{
		int start = 0;
		int end = list.size();
		
		if(pageable.isPaged())
		{
			start = (int) pageable.getOffset();
			end = (int) ((start + pageable.getPageSize()) > list.size() ? list.size()
				  : (start + pageable.getPageSize()));
		}
		
		if(!(list.subList(start, end).isEmpty()))
			return new PageImpl<ImageBean>(list.subList(start, end), pageable, list.size());
		else
			return null;
	}

}
